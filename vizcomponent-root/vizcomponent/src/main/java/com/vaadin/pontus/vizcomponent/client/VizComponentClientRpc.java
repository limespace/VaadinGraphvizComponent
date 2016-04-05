package com.vaadin.pontus.vizcomponent.client;

import com.vaadin.shared.communication.ClientRpc;

// ClientRpc is used to pass events from server to client
// For sending information about the changes to component state, use State instead
public interface VizComponentClientRpc extends ClientRpc {

    public void centerToNode(String nodeId);

    public void fitGraph();

    public void centerGraph();

    public void addNodeCss(String nodeId, String property, String value);

    public void removeNodeCss(String nodeId, String property);

    public void addNodeTextCss(String nodeId, String property, String value);

    public void addEdgeCss(String edgeId, String property, String value);

    public void addEdgeTextCss(String edgeId, String property, String value);

}