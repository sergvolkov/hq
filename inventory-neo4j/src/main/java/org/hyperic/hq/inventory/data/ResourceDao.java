package org.hyperic.hq.inventory.data;

import java.util.Set;

import org.hyperic.hq.inventory.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 
 * Repository for lookup of {@link Resource}s
 * @author jhickey
 * @author dcruchfield
 * 
 */
public interface ResourceDao extends GenericDao<Resource> {
    
    /**
     * @param propertyName The name of the property. The property must be
     *        indexed for lookup to succeed
     * @param propertyValue The value to search for
     * @return The count of search results
     */
    int countByIndexedProperty(String propertyName, Object propertyValue);
    
    /**
     * NOTE: Sorting on a field that contains tokenized values (for example "127.0.0.1") is
     * NOT SUPPORTED
     * @param propertyName The name of the property. The property must be
     *        indexed for lookup to succeed (set indexed to true on
     *        PropertyType)
     * @param propertyValue The value to search for
     * @param pageInfo Info on paging and sorting
     * @param sortAttributeType The class type of the property we are sorting by
     *        (as specified by pageInfo.getSort()). Can be null if pageInfo has
     *        no sort
     * @return A paged list of Resource search results
     */
    Page<Resource> findByIndexedProperty(String propertyName, Object propertyValue,
                                         Pageable pageInfo, Class<?> sortAttributeType);

    /**
     * 
     * @param owner The name of the Resource owner
     * @return All Resources owned by the specified owner
     */
    Set<Resource> findByOwner(String owner);

    /**
     * 
     * @return The root Resource to use when making associations (for traversal
     *         of rootless objs)
     */
    Resource findRoot();

    /**
     * Persists a new Resource
     * @param resource The new resource
     */
    void persist(Resource resource);
    
    /**
     * Persists a new Resource to use as the Root resource for traversal of rootless objects
     * @param resource The new root resource
     */
    void persistRoot(Resource resource);
}