package com.jrj.fund.xcheck.dao;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.crud.CrudDao;

import com.jrj.fund.xcheck.bo.BlockedResources;

@DB(table = "blocked_resources")
public interface BlockedResourcesDao extends CrudDao<BlockedResources, Integer> {
}
