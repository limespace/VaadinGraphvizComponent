package com.vaadin.pontus.vizcomponent.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Widget to display a graph represented by
 * {@link com.vaadin.pontus.vizcomponent.client.VizComponentState
 * VizComponentState}
 *
 * The graph is rendered into an SVG element. When the graph has been rendered,
 * event handlers can be added to nodes and edges, and nodes and edges can be
 * styled using CSS.
 *
 * @author Pontus Boström
 *
 */
public class VizComponentWidget extends FlowPanel {

    private Element svg;
    private HashMap<String, String> svgIdToNodeIdMap;
    private HashMap<String, String> svgIdToEdgeIdMap;
    private HashMap<String, String> nodeIdToSvgIdMap;
    private HashMap<String, String> edgeIdToSvgIdMap;

    public VizComponentWidget() {

        // CSS class-name should not be v- prefixed
        setStyleName("vizcomponent");
        svgIdToNodeIdMap = new HashMap<String, String>();
        svgIdToEdgeIdMap = new HashMap<String, String>();
        nodeIdToSvgIdMap = new HashMap<String, String>();
        edgeIdToSvgIdMap = new HashMap<String, String>();

    }

    public void renderGraph(VizComponentState graph) {
    	
    	svgIdToNodeIdMap.clear();
        svgIdToEdgeIdMap.clear();
        nodeIdToSvgIdMap.clear();
        edgeIdToSvgIdMap.clear();
        
        ArrayList<Edge> connections = graph.graph;
        if (svg != null) {
            getElement().removeChild(svg);
            svg = null;
        }
        if (connections == null || connections.isEmpty()) {
            return;
        }
        int nodeCounter = 1;
        int edgeCounter = 1;
        String connSymbol;
        if ("graph".equals(graph.graphType)) {
            // It is undirected graph
            connSymbol = " -- ";
        } else {
            // It is a digraph
            connSymbol = " -> ";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(graph.graphType);
        builder.append(" ");
        if (graph.name != null) {
            builder.append(graph.name);
        }
        builder.append(" { ");
        if (!graph.params.isEmpty()) {
            writeParameters(graph.params, builder, ";\n");
            builder.append(";\n");
        }
        if (!graph.nodeParams.isEmpty()) {
            builder.append("node ");
            writeParameters(graph.nodeParams, builder);
            builder.append(";");
        }
        if (!graph.edgeParams.isEmpty()) {
            builder.append("edge ");
            writeParameters(graph.edgeParams, builder);
        }
        for (Edge edge : connections) {
            Node source = edge.getSource();
            // Produce a node in case there are parameters for it and it
            // hasn't been processed before
            if (!nodeIdToSvgIdMap.containsKey(source.getId())) {
                String svgNodeId = "node" + nodeCounter++;
                svgIdToNodeIdMap.put(svgNodeId, source.getId());
                nodeIdToSvgIdMap.put(source.getId(), svgNodeId);
                HashMap<String, String> params = source.getParams();
                params.put("id", svgNodeId);
                builder.append(source.getId());
                writeParameters(params, builder);
                builder.append(";\n");
            }
            if (edge.getDest() != null) {
                // Produce an edge
                // Each edge only occurs once
                String svgEdgeId = "edge" + edgeCounter++;
                svgIdToEdgeIdMap.put(svgEdgeId, edge.getId());
                edgeIdToSvgIdMap.put(edge.getId(), svgEdgeId);
                builder.append(source.getId());
                builder.append(connSymbol);
                builder.append(edge.getDest().getId());

                HashMap<String, String> params = edge.getParams();
                params.put("id",svgEdgeId);
                writeParameters(params, builder);
                builder.append(";\n");
            }
        }

        builder.append(" } ");

        try {
            String result = compileSVG(builder.toString());
            getElement().setInnerHTML(result);
            svg = getElement().getFirstChildElement();
            svg.setAttribute("width", "100%");
            svg.setAttribute("height", "100%");
            
        } catch (JavaScriptException e) {
            String result = e.getDescription();
            Label label = new Label(result);
            add(label);
        }
    }

    private void writeParameters(HashMap<String, String> params,
            StringBuilder builder) {
        if (!params.isEmpty()) {
            // Produce parameters
            builder.append("[");
            writeParameters(params, builder, ",");
            builder.append("]");
        }
    }

    private void writeParameters(HashMap<String, String> params,
            StringBuilder builder, String sep) {
        // Produce parameters
        Iterator<String> it = params.keySet().iterator();
        String p = it.next();
        String v = params.get(p);
        builder.append(p);
        builder.append("=");
        builder.append(v);
        while (it.hasNext()) {
            builder.append(sep);
            p = it.next();
            v = params.get(p);
            builder.append(p);
            builder.append("=");
            builder.append(v);
        }
    }
        
    private static native String compileSVG(String graph)
    /*-{
          var result = $wnd.Viz(graph, { format: "svg" });
          return result;
        }-*/;

    public void addNodeClickHandler(final VizClickHandler handler) {
        if (svg == null) {
            return;
        }
        addClickHandler(svgIdToNodeIdMap.keySet(), handler);
    }

    private void addClickHandler(Set<String> ids, final VizClickHandler handler) {
        for (String nodeId : ids) {
            Element svgNode = DOM.getElementById(nodeId);
            Event.sinkEvents(svgNode, Event.ONCLICK);
            Event.setEventListener(svgNode, new EventListener() {

                @Override
                public void onBrowserEvent(Event event) {
                    handler.onClick(event);
                }

            });
        }
    }

    public void addEdgeClickHandler(final VizClickHandler handler) {
        if (svg == null) {
            return;
        }
        addClickHandler(svgIdToEdgeIdMap.keySet(), handler);
    }

    public String getNodeId(Element e) {
        String id = e.getAttribute("id");
        return svgIdToNodeIdMap.get(id);
    }

    public String getEdgeId(Element e) {
        String id = e.getAttribute("id");
        return svgIdToEdgeIdMap.get(id);
    }

    public void addNodeCss(String nodeId, String property, String value) {
        if (svg != null) {
            // Style the polygon or ellipse that make up the node
            // In case some other shape then nothing happens
            String id = nodeIdToSvgIdMap.get(nodeId);
            Element svgNode = DOM.getElementById(id);
            applyCssToElement(svgNode, property, value);
        }
    }

    private void applyCssToElement(Element svgNode, String property,
            String value) {
        NodeList<Element> children = svgNode.getElementsByTagName("polygon");
        if (children.getLength() > 0) {
            for (int i = 0; i < children.getLength(); i++) {
                Element child = children.getItem(i);
                child.getStyle().setProperty(property, value);
            }
        } else {
            children = svgNode.getElementsByTagName("ellipse");
            for (int i = 0; i < children.getLength(); i++) {
                Element child = children.getItem(i);
                child.getStyle().setProperty(property, value);
            }
        }
    }

    public void addNodeTextCss(String nodeId, String property, String value) {
        if (svg != null) {
            // Style the text in node
            String id = nodeIdToSvgIdMap.get(nodeId);
            Element svgNode = DOM.getElementById(id);
            applyTextCssToElement(svgNode, property, value);
        }
    }

    public void addEdgeTextCss(String edgeId, String property, String value) {
        if (svg != null) {
            // Style the text in node
            String id = edgeIdToSvgIdMap.get(edgeId);
            Element svgNode = DOM.getElementById(id);
            applyTextCssToElement(svgNode, property, value);
        }
    }

    private void applyTextCssToElement(Element svgNode, String property,
            String value) {
        NodeList<Element> children = svgNode.getElementsByTagName("text");
        for (int i = 0; i < children.getLength(); i++) {
            Element child = children.getItem(i);
            child.getStyle().setProperty(property, value);
        }
    }

    public void addEdgeCss(String edgeId, String property, String value) {
        if (svg != null) {
            // Style the path and polygon that make up the node
            String id = edgeIdToSvgIdMap.get(edgeId);
            Element svgNode = DOM.getElementById(id);
            applyCssToElement(svgNode, property, value);

            NodeList<Element> children = svgNode.getElementsByTagName("path");
            for (int i = 0; i < children.getLength(); i++) {
                Element child = children.getItem(i);
                child.getStyle().setProperty(property, value);
            }

        }
    }

    public void updateSvgSize() {
        if (svg == null) {
            return;
        }
        svg.setAttribute("width", getOffsetWidth() + "px");
        svg.setAttribute("height", getOffsetHeight() + "px");
    }

}