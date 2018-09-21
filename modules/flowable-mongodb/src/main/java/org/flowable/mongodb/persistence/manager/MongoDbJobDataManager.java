/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.mongodb.persistence.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.conversions.Bson;
import org.flowable.common.engine.impl.Page;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.job.api.Job;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.JobQueryImpl;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntityImpl;
import org.flowable.job.service.impl.persistence.entity.data.JobDataManager;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;

/**
 * @author Joram Barrez
 */
public class MongoDbJobDataManager extends AbstractMongoDbDataManager<JobEntity> implements JobDataManager {

    public static final String COLLECTION_JOBS = "jobs";
    
    protected JobServiceConfiguration jobServiceConfiguration;
    
    public MongoDbJobDataManager() {
        
    }
    
    public MongoDbJobDataManager(JobServiceConfiguration jobServiceConfiguration) {
        this.jobServiceConfiguration = jobServiceConfiguration;
    }
    
    @Override
    public String getCollection() {
        return COLLECTION_JOBS;
    }
    
    @Override
    public JobEntity create() {
        return new JobEntityImpl();
    }

    @Override
    public BasicDBObject createUpdateObject(Entity entity) {
        JobEntity jobEntity = (JobEntity) entity;
        BasicDBObject updateObject = null;
        updateObject = setUpdateProperty(jobEntity, "retries", jobEntity.getRetries(), updateObject);
        updateObject = setUpdateProperty(jobEntity, "exceptionMessage", jobEntity.getExceptionMessage(), updateObject);
        updateObject = setUpdateProperty(jobEntity, "lockOwner", jobEntity.getLockOwner(), updateObject);
        updateObject = setUpdateProperty(jobEntity, "lockExpirationTime", jobEntity.getLockExpirationTime(), updateObject);
        return updateObject;
    }

    @Override
    public List<JobEntity> findJobsToExecute(Page page) {
        Bson filter = null;
        if (jobServiceConfiguration.getJobExecutionScope() == null) {
            filter = Filters.and(Filters.eq("scopeType", null), Filters.eq("lockExpirationTime", null));
            
        } else if (!jobServiceConfiguration.getJobExecutionScope().equals("all")){
            filter = Filters.and(Filters.eq("scopeType", jobServiceConfiguration.getJobExecutionScope()), 
                    Filters.eq("lockExpirationTime", null));
            
        } else {
            filter = Filters.eq("lockExpirationTime", null);
        }
        return getMongoDbSession().find(COLLECTION_JOBS, filter, null, 1);
    }

    @Override
    public List<JobEntity> findJobsByExecutionId(String executionId) {
        Bson filter = Filters.eq("executionId", executionId);
        return getMongoDbSession().find(COLLECTION_JOBS, filter);
    }

    @Override
    public List<JobEntity> findJobsByProcessInstanceId(String processInstanceId) {
        Bson filter = Filters.eq("processInstanceId", processInstanceId);
        return getMongoDbSession().find(COLLECTION_JOBS, filter);
    }

    @Override
    public List<JobEntity> findExpiredJobs(Page page) {
        Bson filter = null;
        if (jobServiceConfiguration.getJobExecutionScope() == null) {
            filter = Filters.eq("scopeType", null);
            
        } else if (!jobServiceConfiguration.getJobExecutionScope().equals("all")){
            filter = Filters.eq("scopeType", jobServiceConfiguration.getJobExecutionScope());
            
        }
        
        Date now = jobServiceConfiguration.getClock().getCurrentTime();
        Date maxTimeout = new Date(now.getTime() - jobServiceConfiguration.getAsyncExecutorResetExpiredJobsMaxTimeout());
        
        filter = Filters.and(filter, Filters.or(Filters.lt("lockExpirationTime", now), 
                Filters.and(Filters.eq("lockExpirationTime", null), Filters.lt("createTime", maxTimeout))));
        
        return getMongoDbSession().find(COLLECTION_JOBS, filter, null, 1);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetExpiredJob(String jobId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery) {
        List<Bson> andFilters = new ArrayList<>();
        if (jobQuery.getExecutionId() != null) {
            andFilters.add(Filters.eq("executionId", jobQuery.getExecutionId()));
        }
        
        if (jobQuery.getProcessInstanceId() != null) {
            andFilters.add(Filters.eq("processInstanceId", jobQuery.getProcessInstanceId()));
        }
        
        Bson filter = null;
        if (andFilters.size() > 0) {
            filter = Filters.and(andFilters.toArray(new Bson[andFilters.size()]));
        }
        
        return getMongoDbSession().find(COLLECTION_JOBS, filter);
    }

    @Override
    public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
        List<Bson> andFilters = new ArrayList<>();
        if (jobQuery.getExecutionId() != null) {
            andFilters.add(Filters.eq("executionId", jobQuery.getExecutionId()));
        }
        
        if (jobQuery.getProcessInstanceId() != null) {
            andFilters.add(Filters.eq("processInstanceId", jobQuery.getProcessInstanceId()));
        }
        
        Bson filter = null;
        if (andFilters.size() > 0) {
            filter = Filters.and(andFilters.toArray(new Bson[andFilters.size()]));
        }
        
        return getMongoDbSession().count(COLLECTION_JOBS, filter);
    }

    @Override
    public void deleteJobsByExecutionId(String executionId) {
        List<JobEntity> jobs = findJobsByExecutionId(executionId);
        for (JobEntity jobEntity : jobs) {
            delete(jobEntity);
        }
    }

}