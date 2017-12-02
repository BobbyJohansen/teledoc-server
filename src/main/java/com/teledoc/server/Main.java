package com.teledoc.server;

import java.util.List;
import java.util.UUID;

import eu.hgross.blaubot.core.Blaubot;
import eu.hgross.blaubot.core.BlaubotFactory;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;

public class Main {
	public static final UUID TELEDOC_UUID = UUID.fromString("3ef00e98-a42e-4d71-acc7-c9d5bde24c90");
	
	public static void main(String[] args) {
		System.out.println("Teledoc server startup");
		System.out.println("Teledoc UUID: " + TELEDOC_UUID);

		Main main = new Main();
		main.startNetworking();
	}
	
	public void startNetworking() {
		System.out.println("startNetworking >>>");
		Blaubot bb = BlaubotFactory.createEthernetBlaubot(TELEDOC_UUID);

		// create the channel
		final IBlaubotChannel channel = bb.createChannel((short)1);

        channel.publish("Hello world!".getBytes());

		channel.subscribe(new IBlaubotMessageListener() {
			@Override
		    public void onMessage(BlaubotMessage message) {
		        String msg = new String(message.getPayload());
		        System.out.println("msg received: " + msg);
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
