/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hibernate.dao;

import java.io.Serializable;
import java.util.Collection;

import org.hibernate.Session;
import org.hyperic.hq.authz.AuthzSubject;
import org.hyperic.hq.authz.ResourceType;
import org.hyperic.hq.authz.shared.ResourceTypeValue;

/**
 * CRUD methods, finders, etc. for ResourceType
 */
public class ResourceTypeDAO extends HibernateDAO
{
    public ResourceTypeDAO(Session session) {
        super(ResourceType.class, session);
    }

    public ResourceType create(AuthzSubject creator,
                               ResourceTypeValue createInfo) {
        ResourceType res = new ResourceType(createInfo);
        save(res);
        return res;
    }

    public ResourceType findById(Integer id) {
        return (ResourceType) super.findById(id);
    }

    public Collection findAll() {
        return (Collection) super.findAll();
    }

    public void save(ResourceType entity) {
        super.save(entity);
    }

    public ResourceType merge(ResourceType entity) {
        return (ResourceType) super.merge(entity);
    }

    public void remove(AuthzSubject whoami, ResourceType entity) {
        // XXX need to check against owner
        super.remove(entity);
    }

    public void evict(ResourceType entity) {
        super.evict(entity);
    }

    public ResourceType findByName(String name)
    {
        String sql = "from ResourceType where name=?";
        return (ResourceType)getSession().createQuery(sql)
            .setString(0, name)
            .uniqueResult();
    }
}
