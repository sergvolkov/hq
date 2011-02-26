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
package org.hyperic.hq.escalation.server.session;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hyperic.hq.auth.domain.AuthzSubject;

/**
 * The escalation state ties an escalation chain to an alert definition. 
 */
@Entity
@Table(name="EAM_ESCALATION_STATE", uniqueConstraints = { @UniqueConstraint(name = "alert_def_id_key", columnNames = { "ALERT_DEF_ID","ALERT_TYPE"
 }) })
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class EscalationState implements Serializable
{
    @Id
    @GenericGenerator(name = "mygen1", strategy = "increment")  
    @GeneratedValue(generator = "mygen1")  
    @Column(name = "ID")
    private Integer id;

    @Column(name="VERSION_COL",nullable=false)
    @Version
    private Long version;
    
    @Column(name="NEXT_ACTION_IDX",nullable=false)
    private int nextAction;

    @Column(name="NEXT_ACTION_TIME",nullable=false)
    private long nextActionTime;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ESCALATION_ID",nullable=false)
    private Escalation escalation;
    
    @SuppressWarnings("unused")
    @Column(name="ALERT_TYPE",nullable=false)
    private int alertTypeEnum;
    
    private transient EscalationAlertType alertType;
    
    @Column(name="ALERT_DEF_ID",nullable=false)
    private int alertDefId;
    
    @Column(name="ALERT_ID",nullable=false)
    private int alertId;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACKNOWLEDGED_BY")
    @Index(name="ACKNOWLEDGED_BY_IDX")
    private AuthzSubject acknowledgedBy;
    
    private transient boolean paused;
    
    protected EscalationState(){
    }

    protected EscalationState(Escalatable alert) {
        PerformsEscalations def = alert.getDefinition();
        
        escalation     = def.getEscalation();
        nextAction     = 0;
        nextActionTime = System.currentTimeMillis();
        alertDefId     = def.getId().intValue();
        alertType      = def.getAlertType();
        alertId        = alert.getId().intValue();
    }

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
    
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getNextAction() {
        return nextAction;
    }

    protected void setNextAction(int nextAction) {
        this.nextAction = nextAction;
    }

    public long getNextActionTime() {
        return nextActionTime;
    }

    protected void setNextActionTime(long nextActionTime) {
        this.nextActionTime = nextActionTime;
    }

    public Escalation getEscalation() {
        return escalation;
    }

    protected void setEscalation(Escalation escalation) {
        this.escalation = escalation;
    }

    public int getAlertDefinitionId() {
        return alertDefId;
    }

    protected void setAlertDefinitionId(int alertDefinitionId) {
        alertDefId = alertDefinitionId;
    }

    public int getAlertId() {
        return alertId;
    }

    protected void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public EscalationAlertType getAlertType() {
        return alertType;
    }

    protected int getAlertTypeEnum() {
        return alertType.getCode();
    }

    protected void setAlertTypeEnum(int typeCode) {
        alertType = EscalationAlertType.findByCode(typeCode);
    }
    
    public AuthzSubject getAcknowledgedBy() {
        return acknowledgedBy;
    }
    
    protected void setAcknowledgedBy(AuthzSubject subject) {
        acknowledgedBy = subject;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof EscalationState)) {
            return false;
        }
        Integer objId = ((EscalationState)obj).getId();
  
        return getId() == objId ||
        (getId() != null && 
         objId != null && 
         getId().equals(objId));     
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (getId() != null ? getId().hashCode() : 0);
        return result;      
    }
}
