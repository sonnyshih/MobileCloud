package com.sonnyshih.mobilecloud.manage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import com.sonnyshih.mobilecloud.util.StringUtil;

public class BonjourManage {
	public final static String HOSTNAME = "TOGODrive";
	public final static String ROUTER_TYPE = "_router._tcp.local."; // device type
	public final static String DRIVE_TYPE = "_drive._tcp.local."; // device type
	private JmDNS jmDNS = null;
	private BonjourServiceListener bonjourServiceListener;
	
	private BonjourHanlder bonjourHanlder;
	
	public BonjourManage(BonjourHanlder bonjourHanlder){
		this.bonjourHanlder = bonjourHanlder;
	}
		
	public void startBonjour() {

		String ipAddress = ApplicationManager.getInstance().getIpAddress();

		InetAddress inetAddress;

		if (!StringUtil.isEmpty(ipAddress)) {
			try {
				bonjourServiceListener = new BonjourServiceListener();
				inetAddress = InetAddress.getByName(ipAddress);
				jmDNS = JmDNS.create(inetAddress, HOSTNAME);
				jmDNS.addServiceListener(ROUTER_TYPE, bonjourServiceListener);
				jmDNS.addServiceListener(DRIVE_TYPE, bonjourServiceListener);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			bonjourHanlder.doErrorExcute("Can get the ip address");
		}

	}

	
	class BonjourServiceListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			// System.out.println("Service added   : " + event.getName() + "." +
			// event.getType());
			// Log.d("Mylog","Service added   : " + event.getName() + "." +
			// event.getType());
		}

		public void serviceRemoved(ServiceEvent event) {
			// System.out.println("Service removed : " + event.getName() + "." +
			// event.getType());
			// Log.d("Mylog","Service removed : " + event.getName() + "." +
			// event.getType());
		}// End serviceRemoved

		public void serviceResolved(ServiceEvent event) {
			String mobileCloudName = event.getName();
			String ip = event.getInfo().getPropertyString("ip");
			String port = event.getInfo().getPropertyString("port");
			String mac = event.getInfo().getPropertyString("mac");

			if (event.getInfo().getType().equals(ROUTER_TYPE)) {
				bonjourHanlder.doRouterExcute(mobileCloudName, ip, port, mac);
				jmDNS.removeServiceListener(ROUTER_TYPE, bonjourServiceListener);
			}
			
			if (event.getInfo().getType().equals(DRIVE_TYPE)) {
				bonjourHanlder.doDriveExcute(mobileCloudName, ip, port, mac);
				jmDNS.removeServiceListener(DRIVE_TYPE, bonjourServiceListener);
			}
			
		}// End serviceResolved
	}// End SampleListener
	
	
	public interface BonjourHanlder {
		public void doRouterExcute(String mobileCloudName, String routerIp, String routerPort, String routerMac);
		public void doDriveExcute(String mobileCloudName, String driveIp, String drivePort, String driveMac);
		public void doErrorExcute(String errorMessage);
	}

}
