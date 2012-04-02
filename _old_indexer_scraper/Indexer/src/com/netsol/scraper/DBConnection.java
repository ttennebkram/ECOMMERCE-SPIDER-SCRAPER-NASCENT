package com.netsol.scraper;

import javax.transaction.SystemException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by IntelliJ IDEA.
 * User: Hasnain Rashid
 * Date: Jan 27, 2011
 * Time: 11:12:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBConnection {


    public Connection getConnection() throws SystemException{
        //String authType = configBundle.getString("authType");
        String connectionKeyValue = "jdbc:mysql://localhost/smartoci";
        String userName= "smartoci";
        String password= "oci";
        Connection dbConnection = null;
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            dbConnection = DriverManager.getConnection(connectionKeyValue, userName,password);
            if (dbConnection == null){
                throw new SystemException("Can not create connection for connection string: " +connectionKeyValue);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }
}
