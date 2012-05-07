package org.openlcb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collection;
import java.beans.PropertyChangeListener;

/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision$
 */
public class MimicNodeStoreTest extends TestCase {
    MimicNodeStore store = null;
    
    NodeID nid1 = new NodeID(new byte[]{1,3,3,4,5,6});
    NodeID nid2 = new NodeID(new byte[]{2,3,3,4,5,6});
    
    ProducerIdentifiedMessage pim1 = new ProducerIdentifiedMessage(nid1, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}));
                                                
    ProducerIdentifiedMessage pim2 = new ProducerIdentifiedMessage(nid2, 
                                                new EventID(new byte[]{1,0,0,0,0,0,1,0}));
                 
    PropertyChangeListener listener;
    boolean listenerFired;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    NodeID node = new NodeID(new byte[]{1,2,3,4,5,6});
                                   
    public void setUp() {
        store = new MimicNodeStore(connection, node);
        
        listener = new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { listenerFired = true; }
        };
        listenerFired = false;
    }

    public void testCtor() {
        Assert.assertNotNull(store);
    }
    
    public void testListExists() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertNotNull(list);        
    }

    public void testListInitiallyEmpty() {
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==0);        
    }
    
    public void testAcceptsMessage() {
        store.put(pim1,null);
    }
    
    public void testMessageAddsToList() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    public void testMessageMemoKnowsNodeID() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertEquals(list.iterator().next().getNodeID(), nid1);
    }
    
    public void testHandleMultipleNodes() {
        store.put(pim1,null);
        store.put(pim2,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==2);
    }
    
    public void testHandleMultipleMessagesFromNode() {
        store.put(pim1,null);
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        Assert.assertTrue(list.size()==1);
    }
    
    public void testNoDefaultProtocolInfo() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();

        Assert.assertNull(memo.getProtocolIdentification());
    }

    public void testProtocolInfoAvailableFromNode() {
        store.put(pim1,null);
        Collection<MimicNodeStore.NodeMemo> list = store.getNodeMemos();
        MimicNodeStore.NodeMemo memo = list.iterator().next();
        store.put(new ProtocolIdentificationReplyMessage(nid1, 0x03), null);

        Assert.assertNotNull(memo.getProtocolIdentification());
    }
    
    public void testForNotificationOfNode() {
        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 
                MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                memo.addPropertyChangeListener(listener);
            }
        });
        store.put(pim1,null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid1, 0x03), null);
        Assert.assertTrue(listenerFired);
        
    }

    public void testForNotificationOfOnlyOneNode() {
        store.put(pim1,null);

        store.addPropertyChangeListener(
            new PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) { 
                MimicNodeStore.NodeMemo memo = (MimicNodeStore.NodeMemo) e.getNewValue();
                memo.addPropertyChangeListener(listener);
            }
        });
        store.put(pim2,null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid1, 0x03), null);
        Assert.assertFalse(listenerFired);

        store.put(new ProtocolIdentificationReplyMessage(nid2, 0x03), null);
        Assert.assertTrue(listenerFired);
        
    }

    public void testForNotificationOfProtocolIdent() {
        store.addPropertyChangeListener(listener);
        store.put(pim1,null);
        
        Assert.assertTrue(listenerFired);
    }
    // from here down is testing infrastructure
    
    public MimicNodeStoreTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MimicNodeStoreTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MimicNodeStoreTest.class);
        return suite;
    }
}