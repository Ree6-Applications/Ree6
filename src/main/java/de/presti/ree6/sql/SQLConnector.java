package de.presti.ree6.sql;

import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLConnector {

	public String username;
	public String password;
	public String host;
	public String database;
	public int port;
	public Connection con;

	public SQLConnector(String user, String password, String host2, String dB, int port3) {
		Main.sqlConnector = this;
		username = user;
		this.password = password;
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
						password);
				Logger.log("MySQL", "Service (MySQL) wurde gestartet. Verbindung erfolgreich hergestellt");
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

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS TwitchNotify (GID VARCHAR(40), NAME VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}


		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS LogWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS WelcomeWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS NewsWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS RainbowWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS JoinMessage (GID VARCHAR(40), MSG VARCHAR(250))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS MuteRoles (GID VARCHAR(40), RID VARCHAR(40))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS ChatProtector (GID VARCHAR(40), WORD VARCHAR(40))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS AutoRoles (GID VARCHAR(40), RID VARCHAR(40))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS Invites (GID VARCHAR(40), UID VARCHAR(40), USES VARCHAR(40), CODE VARCHAR(40))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS Level (GID VARCHAR(40), UID VARCHAR(40), XP VARCHAR(500))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS VCLevel (GID VARCHAR(40), UID VARCHAR(40), XP VARCHAR(500))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS VCLevelAutoRoles (GID VARCHAR(40), RID VARCHAR(40), LVL VARCHAR(500))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}

		try (PreparedStatement ps = con.prepareStatement(
				"CREATE TABLE IF NOT EXISTS ChatLevelAutoRoles (GID VARCHAR(40), RID VARCHAR(40), LVL VARCHAR(500))")){
			ps.executeUpdate();
		} catch (SQLException ignore) {}
	}

	public void query(String sql) {
		try {
			if (isConnected()) {
				con.createStatement().executeUpdate(sql);
			}
		} catch (Exception ignore) {}
	}

}