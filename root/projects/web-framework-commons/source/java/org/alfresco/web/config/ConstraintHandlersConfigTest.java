/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client
 * config service. These tests only include those that require a single config
 * xml file. Override-related tests, which use multiple config xml files, are
 * located in peer classes in this package.
 * 
 * @author Neil McErlean
 */
public class ConstraintHandlersConfigTest extends BaseTest
{
    protected XMLConfigService configService;
    protected Config globalConfig;
    protected ConfigElement globalDefaultControls;
    protected ConfigElement globalConstraintHandlers;
    protected FormConfigElement formConfigElement;
    protected DefaultControlsConfigElement defltCtrlsConfElement;

    protected String getConfigXmlFile()
    {
        return "test-config-forms.xml";
    }
    
    @SuppressWarnings("unchecked")
	public void testReadConstraintHandlersFromConfigXml()
    {
        // Test that the constraint-handlers' constraints are read from the
        // config file
        Map<String, String> expectedValidationHandlers = new HashMap<String, String>();
        expectedValidationHandlers.put("MANDATORY",
                "Alfresco.forms.validation.mandatory");
        expectedValidationHandlers.put("REGEX",
                "Alfresco.forms.validation.regexMatch");
        expectedValidationHandlers.put("NUMERIC",
                "Alfresco.forms.validation.numericMatch");

        ConstraintHandlersConfigElement chConfigElement
            = (ConstraintHandlersConfigElement) globalConstraintHandlers;
        List<String> actualTypes = chConfigElement.getConstraintTypes();
        assertEquals("Incorrect type count.",
                expectedValidationHandlers.size(), actualTypes.size());
        
        assertEquals(expectedValidationHandlers.keySet(), new HashSet(actualTypes));

        // Test that the types map to the expected validation handler.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedValidationHandlers.get(nextKey);
            String nextActualValue = chConfigElement
                    .getValidationHandlerFor(nextKey);
            assertTrue("Incorrect handler for " + nextKey + ": "
                    + nextActualValue, nextExpectedValue
                    .equals(nextActualValue));
        }

        // Test that the constraint-handlers' messages are read from the config
        // file
        Map<String, String> expectedMessages = new HashMap<String, String>();
        expectedMessages.put("MANDATORY", null);
        expectedMessages.put("REGEX", null);
        expectedMessages.put("NUMERIC", "Test Message");

        // Test that the types map to the expected message.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedMessages.get(nextKey);
            String nextActualValue = chConfigElement.getMessageFor(nextKey);
            assertEquals("Incorrect message for " + nextKey + ".",
                    nextExpectedValue, nextActualValue);
        }

        // Test that the constraint-handlers' message-ids are read from the
        // config file
        Map<String, String> expectedMessageIDs = new HashMap<String, String>();
        expectedMessageIDs.put("MANDATORY", null);
        expectedMessageIDs.put("REGEX", null);
        expectedMessageIDs.put("NUMERIC", "regex_error");

        // Test that the types map to the expected message-id.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedMessageIDs.get(nextKey);
            String nextActualValue = chConfigElement.getMessageIdFor(nextKey);
            assertEquals("Incorrect message-id for " + nextKey + ".",
                    nextExpectedValue, nextActualValue);
        }
        
        // Test that the MANDATORY constraint has the correct event
        assertEquals("Incorrect event for MANDATORY constraint", "blur", 
                    chConfigElement.getEventFor("MANDATORY"));
    }

    public void testConstraintHandlerElementShouldHaveNoChildren()
    {
        try
        {
            ConstraintHandlersConfigElement chConfigElement = (ConstraintHandlersConfigElement) globalConstraintHandlers;
            chConfigElement.getChildren();
            fail("getChildren() did not throw an exception");
        } catch (ConfigException ce)
        {
            // expected exception
        }

    }

    /**
     * Tests the combination of a ConstraintHandlersConfigElement with another that
     * contains additional data.
     */
    public void testCombineConstraintHandlersWithAddedParam()
    {
        ConstraintHandlersConfigElement basicElement = new ConstraintHandlersConfigElement();
        basicElement.addDataMapping("REGEX", "foo.regex", null, null, null);

        // This element is the same as the above, but adds message & message-id.
        ConstraintHandlersConfigElement elementWithAdditions = new ConstraintHandlersConfigElement();
        elementWithAdditions.addDataMapping("REGEX", "foo.regex", "msg", "msg-id", null);

        ConstraintHandlersConfigElement combinedElem
                = (ConstraintHandlersConfigElement)basicElement.combine(elementWithAdditions);
        
        assertEquals("foo.regex", combinedElem.getItems().get("REGEX").getValidationHandler());
        assertEquals("msg", combinedElem.getItems().get("REGEX").getMessage());
        assertEquals("msg-id", combinedElem.getItems().get("REGEX").getMessageId());
        assertEquals(null, combinedElem.getItems().get("REGEX").getEvent());
    }

    /**
     * Tests the combination of a ConstraintHandlersConfigElement with another that
     * contains modified data.
     */
    public void testCombineConstraintHandlersWithModifiedParam()
    {
        ConstraintHandlersConfigElement initialElement = new ConstraintHandlersConfigElement();
        initialElement.addDataMapping("REGEX", "foo.regex", null, null, null);

        // This element is the same as the above, but adds message & message-id.
        ConstraintHandlersConfigElement modifiedElement = new ConstraintHandlersConfigElement();
        modifiedElement.addDataMapping("REGEX", "bar.regex", "msg", "msg-id", null);

        ConstraintHandlersConfigElement combinedElem
                = (ConstraintHandlersConfigElement)initialElement.combine(modifiedElement);

        assertEquals("bar.regex", combinedElem.getItems().get("REGEX").getValidationHandler());
        assertEquals("msg", combinedElem.getItems().get("REGEX").getMessage());
        assertEquals("msg-id", combinedElem.getItems().get("REGEX").getMessageId());
        assertEquals(null, combinedElem.getItems().get("REGEX").getEvent());
    }
    
    /**
     * Tests the combination of a ConstraintHandlersConfigElement with another that
     * contains deleted data.
     */
    public void testCombineConstraintHandlersWithDeletedParam()
    {
        ConstraintHandlersConfigElement initialElement = new ConstraintHandlersConfigElement();
        initialElement.addDataMapping("REGEX", "bar.regex", "msg", "msg-id", null);

        // This element is the same as the above, but adds message & message-id.
        ConstraintHandlersConfigElement modifiedElement = new ConstraintHandlersConfigElement();
        modifiedElement.addDataMapping("REGEX", "bar.regex", null, null, null);

        ConstraintHandlersConfigElement combinedElem
                = (ConstraintHandlersConfigElement)initialElement.combine(modifiedElement);

        assertEquals("bar.regex", combinedElem.getItems().get("REGEX").getValidationHandler());
        assertEquals(null, combinedElem.getItems().get("REGEX").getMessage());
        assertEquals(null, combinedElem.getItems().get("REGEX").getMessageId());
        assertEquals(null, combinedElem.getItems().get("REGEX").getEvent());
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        configService = initXMLConfigService(getConfigXmlFile());
        assertNotNull("configService was null.", configService);
    
        Config contentConfig = configService.getConfig("content");
        assertNotNull("contentConfig was null.", contentConfig);
    
        ConfigElement confElement = contentConfig.getConfigElement("form");
        assertNotNull("confElement was null.", confElement);
        assertTrue("confElement should be instanceof FormConfigElement.",
                confElement instanceof FormConfigElement);
        formConfigElement = (FormConfigElement) confElement;
    
        globalConfig = configService.getGlobalConfig();
    
        globalDefaultControls = globalConfig
                .getConfigElement("default-controls");
        assertNotNull("global default-controls element should not be null",
                globalDefaultControls);
        assertTrue(
                "config element should be an instance of DefaultControlsConfigElement",
                (globalDefaultControls instanceof DefaultControlsConfigElement));
        defltCtrlsConfElement = (DefaultControlsConfigElement) globalDefaultControls;
    
        globalConstraintHandlers = globalConfig
                .getConfigElement("constraint-handlers");
        assertNotNull("global constraint-handlers element should not be null",
                globalConstraintHandlers);
        assertTrue(
                "config element should be an instance of ConstraintHandlersConfigElement",
                (globalConstraintHandlers instanceof ConstraintHandlersConfigElement));
    }
}
