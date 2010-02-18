/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.action.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Folder contents parameter constraint
 * 
 * @author Roy Wetherall
 */
public class FolderContentsParameterConstraint extends BaseParameterConstraint
{
    private NodeService nodeService;
    
    private SearchService searchService;
    
    private DictionaryService dictionaryService;
    
    private String searchPath;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setSearchPath(String searchPath)
    {
        this.searchPath = searchPath;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        ResultSet resultSet = searchService.query(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
                SearchService.LANGUAGE_LUCENE, 
                "PATH:\"" + searchPath + "\"");
        NodeRef rootFolder = null;
        if (resultSet.length() == 0)
        {
            throw new AlfrescoRuntimeException("The path '" + searchPath + "' did not return any results.");
        }
        else
        {
            rootFolder = resultSet.getNodeRef(0);
        }
        
        Map<String, String> result = new HashMap<String, String>(23);
        buildMap(result, rootFolder);        
        return result;
    }        
    
    private void buildMap(Map<String, String> result, NodeRef folderNodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(folderNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef nodeRef = assoc.getChildRef();
            QName className = nodeService.getType(nodeRef);
            if (dictionaryService.isSubClass(className, ContentModel.TYPE_CONTENT) == true)
            {
                result.put(nodeRef.toString(), 
                        (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
            }
            else if (dictionaryService.isSubClass(className, ContentModel.TYPE_FOLDER) == true)
            {
                buildMap(result, nodeRef);
            }
        }
    }
}
