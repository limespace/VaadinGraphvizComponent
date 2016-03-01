package com.vaadin.pontus.vizcomponent.model;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to describe the graphs rendered by the VizComponent. A
 * graph consists of a set of Nodes connected by Edges. Nodes and Edges have
 * id:s that need to be unique. That is, each node needs an unique id among the
 * nodes and each edge needs a unique id among the edges. The id:s need to be
 * valid <a href="http://www.graphviz.org/doc/info/lang.html">dot-language
 * id:s</a>.
 *
 * Nodes and edges can have parameters that determines how they are displayed.
 * These parameters and values are the ones accepted in the <a
 * href="http://www.graphviz.org/doc/info/attrs.html">DOT language with some
 * restrictions</a>.
 *
 * The graphs can also have parameters, they also need to be valid dot
 * parameters. There is at the moment no way to create subgraphs.
 *
 * @author Pontus Boström
 *
 */
public class Subgraph extends Parameterised {

    /**
     * This class represents nodes in a graph. As mentioned above each node
     * needs an unique id. Each node can have a list of parameters
     *
     * @author Pontus Boström
     *
     */
    public static class Node extends GraphElement {

        public Node(String id) {
            super();
            this.id = id;
        }
    }

    public static class GraphNode extends Node {
        private Subgraph subgraph;

        public GraphNode(String id, Subgraph subgraph) {
            super(id);
            this.subgraph = subgraph;
        }

        public Subgraph getGraph() {
            return subgraph;
        }

        /**
         * Sets the parameter for the subgraph
         */
        @Override
        public void setParam(String name, String value) {
            subgraph.setParam(name, value);
        }

        /**
         * Gets the parameter for the subgraph
         */
        @Override
        public String getParam(String name) {
            return subgraph.getParam(name);
        }

        /**
         * Returns the set of defined parameters for the subgraph
         */
        @Override
        public Set<String> getParams() {
            return subgraph.getParams();
        }
    }

    /**
     * This class represents edges in a graph. As mentioned above each edge
     * needs an unique id. Each edge can have a list of parameters
     *
     * @author Pontus Boström
     *
     */
    public static class Edge extends GraphElement {
        private static volatile long counter = 0L;
        private Node dest;

        public Edge() {
            super();
            id = "edge" + counter++;
        }

        public Node getDest() {
            return dest;
        }

        public void setDest(Node dest) {
            this.dest = dest;
        }

    }

    final private Map<Node, Set<AbstractMap.SimpleEntry<Node, Edge>>> graph;
    private Parameterised nodeParams;
    private Parameterised edgeParams;
    private Map<String, Node> nodeMap;
    private Map<String, Edge> edgeMap;

    /**
     * Constructs an empty graph with no nodes and no edges
     *
     * @param name
     *            an arbitrary name for the graph. This needs to valid dot id.
     * @param type
     *            is either a digraph or graph
     */
    public Subgraph() {
        super();
        graph = new HashMap<Node, Set<AbstractMap.SimpleEntry<Node, Edge>>>();
        nodeParams = new Parameterised();
        edgeParams = new Parameterised();
        nodeMap = new HashMap<String, Node>();
        edgeMap = new HashMap<String, Edge>();
    }

    /**
     * Adds a node to the graph
     *
     * @param node
     *            the node to add
     */
    public void addNode(Node node) {
        nodeMap.put(node.getId(), node);
        graph.put(node, new HashSet<AbstractMap.SimpleEntry<Node, Edge>>());
    }

    /**
     * Adds an edge between the nodes. If the nodes are not in the graph
     * already, they are added
     *
     * @param source
     * @param dest
     */
    public void addEdge(Node source, Node dest) {
        Edge edge = new Edge();
        edgeMap.put(edge.getId(), edge);
        AbstractMap.SimpleEntry<Node, Edge> edgeDest = new AbstractMap.SimpleEntry<Node, Edge>(
                dest, edge);
        if (!graph.containsKey(dest)) {
            nodeMap.put(dest.getId(), dest);
            graph.put(dest, new HashSet<AbstractMap.SimpleEntry<Node, Edge>>());
        }
        Set<AbstractMap.SimpleEntry<Node, Edge>> destSet = graph.get(source);
        if (destSet == null) {
            nodeMap.put(source.getId(), source);
            destSet = new HashSet<AbstractMap.SimpleEntry<Node, Edge>>();
            destSet.add(edgeDest);
            graph.put(source, destSet);
        } else {
            destSet.add(edgeDest);
        }
    }

    /**
     * Returns the set of nodes in the graph
     *
     * @return an empty set if there are no nodes
     */
    public Set<Node> getNodes() {
        return graph.keySet();
    }

    /**
     * Returns the set of outgoing edges from the node
     *
     * @param node
     * @return empty set if there are no edges
     */
    public Set<AbstractMap.SimpleEntry<Node, Edge>> getConnections(Node node) {
        return graph.get(node);
    }

    /**
     * Returns the edge between the two nodes, starting at source. Note that
     * there can be an edges in the other direction too, this one is never
     * returned
     *
     * @param source
     * @param dest
     * @return null if there is no edge between the nodes
     */
    public Edge getEdge(Node source, Node dest) {
        Set<AbstractMap.SimpleEntry<Node, Edge>> destSet = graph.get(source);
        if (destSet == null || destSet.isEmpty()) {
            return null;
        }
        for (AbstractMap.SimpleEntry<Node, Edge> pair : destSet) {
            if (pair.getKey().equals(dest)) {
                return pair.getValue();
            }
        }
        return null;
    }

    /**
     * Return the set of all edges in the graph
     *
     * @return an empty set if there are no edges
     */
    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<Edge>();
        for (String id : edgeMap.keySet()) {
            edges.add(edgeMap.get(id));
        }
        return edges;
    }

    /**
     * Set a parameter that applies to all nodes in the graph. The name and
     * value must specify a valid graphviz attribute value pair.
     *
     * @param name
     * @param value
     */
    public void setNodeParameter(String name, String value) {
        nodeParams.setParam(name, value);
    }

    /**
     * Get the value of the parameter
     *
     * @param name
     * @return null if the parameter is not set
     */
    public String getNodeParam(String name) {
        return nodeParams.getParam(name);
    }

    /**
     * Get all parameter names that have been set
     *
     * @return an empty set if no parameters have been set.
     */
    public Set<String> getNodeParams() {
        return nodeParams.getParams();
    }

    /**
     * Get the node with the corresponding id
     *
     * @param nodeId
     * @return null if there is no such node
     */
    public Node getNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    /**
     * Get the edge with the corresponding id
     *
     * @param edgeId
     * @return null if there is no such node
     */
    public Edge getEdge(String edgeId) {
        return edgeMap.get(edgeId);
    }

    /**
     * Set the parameter with the given name to the given value. This applies to
     * all edges. The parameter must be a valid dot parameter.
     *
     * @param name
     * @param value
     */
    public void setEdgeParameter(String name, String value) {
        edgeParams.setParam(name, value);
    }

    /**
     * Get the edge parameter value corresponding to the parameter name.
     *
     * @param name
     * @return null if the parameter is not set
     */
    public String getEdgeParam(String name) {
        return edgeParams.getParam(name);
    }

    /**
     * Get the set of edge parameters that have been set-
     *
     * @return an empty set if no parameters have been set
     */
    public Set<String> getEdgeParams() {
        return edgeParams.getParams();
    }

    /**
     * Removes the node from the graph. All incoming and outgoing edges are also
     * removed
     *
     * @param node
     */
    public void remove(Node node) {
        if (!graph.containsKey(node)) {
            return;
        }
        nodeMap.remove(node.getId());
        // Remove all outgoing edges
        Set<AbstractMap.SimpleEntry<Node, Edge>> connections = graph.get(node);
        graph.remove(node);
        for (AbstractMap.SimpleEntry<Node, Edge> pair : connections) {
            edgeMap.remove(pair.getValue().getId());
        }
        // Remove all incoming edges
        for (Node otherNode : graph.keySet()) {
            connections = graph.get(otherNode);
            Iterator<AbstractMap.SimpleEntry<Node, Edge>> pairIt = connections
                    .iterator();
            while (pairIt.hasNext()) {
                AbstractMap.SimpleEntry<Node, Edge> pair = pairIt.next();
                if (pair.getKey().equals(node)) {
                    edgeMap.remove(pair.getValue().getId());
                    pairIt.remove();
                }
            }
        }

    }

    /**
     * Remove the edge from the graph. Nodes are not removed even if the become
     * unconnected.
     *
     * @param edge
     */
    public void remove(Edge edge) {
        edgeMap.remove(edge.getId());
        for (Node node : graph.keySet()) {
            Set<AbstractMap.SimpleEntry<Node, Edge>> connections = graph
                    .get(node);
            Iterator<AbstractMap.SimpleEntry<Node, Edge>> pairIt = connections
                    .iterator();
            while (pairIt.hasNext()) {
                AbstractMap.SimpleEntry<Node, Edge> pair = pairIt.next();
                if (pair.getValue().equals(edge)) {
                    pairIt.remove();
                    return;// One edge can only be in one place
                }
            }
        }
    }

}
