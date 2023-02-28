package com.photon.network;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.discord.DiscordEngine;
import com.photon.informations.PhotonInfosManager;
import com.photon.ui.base.Frame;
import com.photon.ui.base.Panel;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.RememberAllWrittenTextPrintStream;

public class NetworkEngine {

	public static void main(final String[] args) throws InterruptedException {
		PhotonEngine.setIP(PhotonInfosManager.getCurrentIP());
		boolean gui = true;
		for(String arg : args) {
			if(arg.equalsIgnoreCase("nogui")) gui = false;
		}
		if(gui) new NetworkFrame();
		ConsoleManager.print(EnumLogType.NETWORK, "Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!");
		if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.network_Ip_Local) && !PhotonInfosManager.isIPEquals(PhotonEngine.network_Ip)) {
			System.exit(0);
			return;
		}
		NetworkDirectories.load();
		DiscordEngine.load();
		NetworkConnectionServer.load();
    	if(PhotonInfosManager.hasAPIUpdate(PhotonEngine.VERSION)) {
    		final File file = PhotonInfosManager.updateAPI("Network", NetworkDirectories.baseDirectory.getParentFile(), PhotonEngine.VERSION);
    		if(file !=null && PhotonInfosManager.updateSize > 0 && PhotonInfosManager.updateSizeDownloaded >= PhotonInfosManager.updateSize) System.exit(0);
    	}
    }
	
	public static Connection getPlayerConnection(String uuid) { return (uuid == null || uuid.isEmpty()) ? null : PhotonEngine.networkConnectionsList.get(uuid); }
	
	public static String getPlayerUUID(Connection connection) {
        for(Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	if(entry.getValue().equals(connection)) { return entry.getKey(); }
        }
        return "";
    }
	
	public static List<Connection> getConnectedConnection() {
    	List<Connection> list = new ArrayList<>();
        for (Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	final Connection conn = entry.getValue();
            if(conn != null && conn.isConnected()) { list.add(conn); }
        }
        return list;
    }
	
	public static class NetworkFrame extends Frame {
		
		public NetworkFrame() {
			super();
			this.setTitle("Network Panel - " + PhotonEngine.VERSION);
			this.setSize(597, 455);
			this.setResizable(false);
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
			this.setContentPane(new NetworkPanel(this));
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}
	
	public static class NetworkPanel extends Panel {

		final JTextArea console = new JTextArea(24, 80);
		final JScrollPane scroller = new JScrollPane(console);
		
		public NetworkPanel(Frame parent) {
			super(parent);
			this.setBackground(Color.black);
			console.setBackground(Color.BLACK);
			console.setForeground(Color.LIGHT_GRAY);
			console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			console.setEditable(false);
			final RememberAllWrittenTextPrintStream conn = new RememberAllWrittenTextPrintStream(System.out, console);
			System.setErr(conn);
		    System.setOut(conn);
		    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		    scroller.setBorder(null);
		    scroller.getVerticalScrollBar().setBackground(Color.BLACK);
		    scroller.getHorizontalScrollBar().setBackground(Color.BLACK);
		    this.addComponent(scroller);
		}

		@Override
		public void drawPanel(Graphics g) {}	
	}
}
