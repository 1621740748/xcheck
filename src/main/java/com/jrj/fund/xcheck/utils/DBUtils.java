package com.jrj.fund.xcheck.utils;

import javax.sql.DataSource;

import org.jfaster.mango.datasource.DriverManagerDataSource;
import org.jfaster.mango.operator.Mango;

import com.jrj.fund.xcheck.config.DBConfig;

public class DBUtils {
	private static Mango mango=null;
	public static Mango getInstance() {
		if(mango!=null) {
			return mango;
		}
	    DataSource ds = new DriverManagerDataSource(DBConfig.driverClassName, DBConfig.url, DBConfig.username, DBConfig.password);
	    mango = Mango.newInstance(ds); // 使用数据源初始化m
	    return mango;
	}

}
