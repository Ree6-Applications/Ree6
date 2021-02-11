package de.presti.ree6.sql;

import de.presti.ree6.main.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {

	public String username;
	public String passwort;
	public String databse;
	public String host;
	public String database;
	public int port = 3306;
	public Connection con;

	public SQLConnector(String user, String password, String host2, String dB, int port3) {
		Main.sqlConnector = this;
		username = user;
		passwort = password;
		database = dB;
		host = host2;
		port = port3;
		connect();
		createTables();
	}

	public void connect() {
		if (!isConnected()) {
			try {
				con = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username,
						passwort);
				System.out.println("Service (MySQL) wurde gestartet. Verbindung erfolgreich hergestellt");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void close() {
		if (!isConnected()) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isConnected() {

		return con != null;

	}

	public boolean isConnected2() {
		try {
			return !con.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	public void createTables() {
		try {
			java.sql.PreparedStatement st = null;
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS LogWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))");
			st.executeUpdate();
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS WelcomeWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))");
			st.executeUpdate();
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS MuteRoles (GID VARCHAR(40), RID VARCHAR(40))");
			st.executeUpdate();
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS AutoRoles (GID VARCHAR(40), RID VARCHAR(40))");
			st.executeUpdate();
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS Invites (GID VARCHAR(40), UID VARCHAR(40), USES VARCHAR(40), CODE VARCHAR(40))");
			st.executeUpdate();
			st = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS Level (GID VARCHAR(40), UID VARCHAR(40), XP VARCHAR(500))");
			st.executeUpdate();

			st.close();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public void query(String sql) {
		try {
			if (isConnected()) {
				con.createStatement().executeUpdate(sql);
			}
		} catch (Exception ex) {}
	}

}