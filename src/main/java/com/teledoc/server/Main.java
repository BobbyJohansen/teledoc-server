package com.teledoc.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import com.google.gson.Gson;
import com.teledoc.common.communication.TeleDocMessage;

import eu.hgross.blaubot.core.Blaubot;
import eu.hgross.blaubot.core.BlaubotFactory;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;

public class Main {
	public static final UUID TELEDOC_UUID = UUID.fromString("3ef00e98-a42e-4d71-acc7-c9d5bde24c90");
	
	public final EntityManagerFactory mEmFactory;
	
	public static void main(String[] args) {
		System.out.println("Teledoc server startup");
		System.out.println("Teledoc UUID: " + TELEDOC_UUID);
		
		Main main = new Main();
		main.connectDb();
		main.startNetworking();
	}
	
	public Main() {
		mEmFactory = Persistence.createEntityManagerFactory("default");
	}
	
	public void connectDb() {
		EntityManager em = mEmFactory.createEntityManager();
		
		if (1 == 1 ) {
			return;
		}
		em.getTransaction().begin();
	    List<TeleDocMessage> msgs = em.createQuery("SELECT m FROM TeleDocMessage m ORDER BY m.timestamp", TeleDocMessage.class).getResultList();
	    for (TeleDocMessage m : msgs) {
	    	System.out.println("msg: " + m.getUuid() + "@" + m.getTimestamp() + ":" + new Gson().toJson(m.getData()));
	    }
		
		TeleDocMessage tdm = new TeleDocMessage();
		Random r = new Random();
		ArrayList<Double> dbls = new ArrayList<>();
		for (int i = 0 ; i < 100; i++) {
			dbls.add(r.nextDouble() * 10);
		}
		tdm.setData(dbls);
		em.persist(tdm);
		em.getTransaction().commit();
	}
	
	public void startNetworking() {
		System.out.println("startNetworking >>>");
		Blaubot bb = BlaubotFactory.createEthernetBlaubot(TELEDOC_UUID);
		Gson gson = new Gson();

		final IBlaubotChannel rawDataChannel = bb.createChannel((short)1);
		final IBlaubotChannel processedDataChannel = bb.createChannel((short)2);

		//TODO Fix
		InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.22.252:8086", "admin", "password");
		String dbName = "teledocdb";
		influxDB.createDatabase(dbName);
		
		rawDataChannel.subscribe(new IBlaubotMessageListener() {
			@Override
		    public void onMessage(BlaubotMessage message) {
				String msg = new String(message.getPayload());
		        System.out.println("msg received: " + msg);
		        TeleDocMessage tdm = gson.fromJson(msg, TeleDocMessage.class);
				EntityManager em = mEmFactory.createEntityManager();
		        em.getTransaction().begin();
		        em.persist(tdm);
		        em.getTransaction().commit();
//		        processedDataChannel.publish(gson.toJson(msg).getBytes());

		        BatchPoints batchPoints = BatchPoints
	                    .database(dbName)
	                    .tag("async", "true")
	                    .consistency(InfluxDB.ConsistencyLevel.ALL)
	                    .build();
				//create a point object
			    Point point1 = Point.measurement(tdm.getDataType().name())
				                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				                    .addField("value", tdm.getData().get(0))
				                    .tag("user", tdm.getPerson() + "")
				                    .build();
				batchPoints.point(point1);
				influxDB.write(batchPoints);
			}
		});
		
		Thread t = new Thread(() -> {
			while (true) {
				List<IBlaubotDevice> devices = bb.getConnectionManager().getConnectedDevices();
                devices.add(0, bb.getOwnDevice());
				int i = 0;
				System.out.println("Devices:");
				for (IBlaubotDevice d : devices) {
					System.out.println("dev " + i + ": " + d);
					i++;
				}
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
		});
		t.setDaemon(true);
		t.start();
		
		bb.addLifecycleListener(new ILifecycleListener() {
            @Override
            public void onDisconnected() {
                System.out.println("onDisconnected");
            }

            @Override
            public void onDeviceLeft(IBlaubotDevice blaubotDevice) {
                System.out.println("onDeviceLeft " + blaubotDevice);
            }

            @Override
            public void onDeviceJoined(IBlaubotDevice blaubotDevice) {
                System.out.println("onDeviceJoined " + blaubotDevice);
            }

            @Override
            public void onConnected() {
                System.out.println("onConnected");
		    }

            @Override
            public void onPrinceDeviceChanged(IBlaubotDevice oldPrince, IBlaubotDevice newPrince) {
                System.out.println("onPrinceDeviceChanged " + oldPrince + " -> " + newPrince);
            }

            @Override
            public void onKingDeviceChanged(IBlaubotDevice oldKing, IBlaubotDevice newKing) {
                System.out.println("onKingDeviceChanged " + oldKing + " -> " + newKing);
            }
		});

		bb.startBlaubot();
		System.out.println("startNetworking <<<");
	}
}
