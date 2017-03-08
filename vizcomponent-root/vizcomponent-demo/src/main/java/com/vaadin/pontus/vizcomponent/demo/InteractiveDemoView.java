package com.vaadin.pontus.vizcomponent.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickListener;
import com.vaadin.pontus.vizcomponent.client.ZoomSettings;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class InteractiveDemoView extends HorizontalSplitPanel {

    public class NodeInfo {
        String caption;
        String info;

        public NodeInfo(String caption, String info) {
            super();
            this.caption = caption;
            this.info = info;
        }

        public String getCaption() {
            return caption;
        }

        public String getInfo() {
            return info;
        }
    }

    class NodeConnection {
        NodeInfo parent;
        NodeInfo child;

        public NodeConnection(NodeInfo parent, NodeInfo child) {
            super();
            this.parent = parent;
            this.child = child;
        }

        public NodeInfo getParent() {
            return parent;
        }

        public NodeInfo getChild() {
            return child;
        }
    }

    // @formatter:off
	NodeInfo[] nodes = { new NodeInfo ("Node1", "Nodeinfo for Node 1"),
						 new NodeInfo ("Node2", "Nodeinfo for Node 2"),
						 new NodeInfo ("Node3", "Nodeinfo for Node 3"),
						 new NodeInfo ("Node4", "Nodeinfo for Node 4"),
						 new NodeInfo ("Node5", "Nodeinfo for Node 5"),
						 new NodeInfo ("Node6", "Nodeinfo for Node 6"),
						 new NodeInfo ("Node7", "Nodeinfo for Node 7"),
						 new NodeInfo ("Node8", "Nodeinfo for Node 8"),
						 new NodeInfo ("Node9", "Nodeinfo for Node 9"),
						 new NodeInfo ("Node10", "Nodeinfo for Node 10")
						};


	NodeConnection[] connections =   { new NodeConnection(nodes[0], nodes[1]),
					   new NodeConnection(nodes[0], nodes[5]),
					   new NodeConnection(nodes[0], nodes[2]),
					   new NodeConnection(nodes[2], nodes[9]),
					   new NodeConnection(nodes[3], nodes[9]),
					   new NodeConnection(nodes[4], nodes[0]),
					   new NodeConnection(nodes[0], nodes[7]),
					   new NodeConnection(nodes[7], nodes[8]),
					   new NodeConnection(nodes[8], nodes[3]),
					   new NodeConnection(nodes[3], nodes[4])
					};
	// @formatter:on

    private VizComponent graphComponent;
    Graph graph;
    Grid<NodeInfo> grid;
    private Label infoLabel;
    CheckBox chkCenterOnSelect;
    
    String lastSelected = null;
    Object selectSource;
    Map<String, NodeInfo> nodeInfoMap;

    public InteractiveDemoView() {

        setFirstComponent(createNodeListPanel());
        setSecondComponent(createGraphDetailPanel());
        setSplitPosition(25, Unit.PERCENTAGE);
        setSizeFull();
    }

    private Component createNodeListPanel() {
        grid = new Grid<NodeInfo>();
        grid.setSizeFull();
        grid.addColumn(NodeInfo::getCaption).setCaption("Caption");
        grid.addColumn(NodeInfo::getInfo).setCaption("Info");
        ListDataProvider<NodeInfo> container = new ListDataProvider<NodeInfo>(
                Arrays.asList(nodes));
        grid.setDataProvider(container);

        grid.addSelectionListener(new SelectionListener<NodeInfo>() {

            @Override
            public void selectionChange(SelectionEvent<NodeInfo> event) {
                Set<NodeInfo> selected = event.getAllSelectedItems();
                if (selected.size() > 0) {
                    Object obj = selected.iterator().next(); // Quick and Dirty
                                                             // for Sample, just
                                                             // get the first
                                                             // one

                    NodeInfo ni = (NodeInfo) obj;
                    if (lastSelected != null
                            && !lastSelected.equals(ni.getCaption())) {
                        Graph.Node node = graph.getNode(lastSelected);
                        graphComponent.removeCss(node, "fill");
                    }
                    lastSelected = ni.getCaption();
                    Graph.Node node = graph.getNode(lastSelected);
                    graphComponent.addCss(node, "fill", "orange");
                    infoLabel.setValue(ni.getCaption() + " - " + ni.getInfo());
                    if (selectSource == null && chkCenterOnSelect.getValue()) {
                        graphComponent.centerToNode(node);
                    }
                    selectSource = null;
                }

            }
        });
        return grid;
    }

    @Override
    public void attach() {
        super.attach();
        final Graph graph = createGraph();
        graphComponent.drawGraph(graph);
    }

    private Component createGraphDetailPanel() {

        VerticalSplitPanel panel = new VerticalSplitPanel();

        graphComponent = createGraphComponent();

        HorizontalLayout buttonArea = new HorizontalLayout();
        buttonArea.setSpacing(true);
        // buttonArea.setWidth(100, Unit.PERCENTAGE);
        chkCenterOnSelect = new CheckBox("center on select"); 
        chkCenterOnSelect.setValue(true);
        buttonArea.addComponent(chkCenterOnSelect);
        buttonArea.setComponentAlignment(chkCenterOnSelect, Alignment.MIDDLE_CENTER);
        CssLayout buttonGroup = new CssLayout();
        buttonGroup.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        
        
        buttonGroup.addComponent(new Button("Center",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        graphComponent.centerGraph();
                    }
                }));

        buttonGroup.addComponent(new Button("Fit", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                graphComponent.fitGraph();
            }
        }));

        buttonGroup.addComponent(new Button("Recreate",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        final Graph graph = createGraph();
                        graphComponent.drawGraph(graph);
                    }
                }));

        buttonArea.addComponent(buttonGroup);
        VerticalLayout pane = new VerticalLayout();
        pane.setSizeFull();

        pane.addComponent(buttonArea);
        pane.setComponentAlignment(buttonArea, Alignment.MIDDLE_RIGHT);
        pane.addComponent(graphComponent);
        pane.setExpandRatio(graphComponent, 1);

        panel.setSplitPosition(80, Unit.PERCENTAGE);
        panel.setSizeFull();
        panel.setFirstComponent(pane);
        infoLabel = new Label("nothing Selected");
        panel.setSecondComponent(infoLabel);
        
        return panel;
    }

    private VizComponent createGraphComponent() {

        final VizComponent component = new VizComponent();
        ZoomSettings zs = new ZoomSettings();
        zs.setPreventMouseEventsDefault(true);
        component.setPanZoomSettings(zs);
        component.setSizeFull();
        component.addClickListener(new NodeClickListener() {

            @Override
            public void nodeClicked(NodeClickEvent e) {
                Graph.Node node = e.getNode();
                node.getId();
                NodeInfo ni = nodeInfoMap.get(node.getId());
                selectSource = this;
                grid.select(ni);
                selectSource = null;
            }
        });

        return component;
    }

    private Graph createGraph() {

        nodeInfoMap = new HashMap<String, NodeInfo>();
        graph = new Graph("G", Graph.DIGRAPH);
        for (NodeInfo ni : nodes) {

            Graph.Node node = new Graph.Node(ni.getCaption());
            nodeInfoMap.put(node.getId(), ni);
            node.setParam("label", ni.getCaption());
            node.setParam("shape", "box");
            node.setParam("fillcolor", "white");
            graph.addNode(node);
        }

        for (NodeConnection nc : connections) {
            Graph.Node node1 = new Graph.Node(nc.getParent().getCaption());
            Graph.Node node2 = new Graph.Node(nc.getChild().getCaption());

            Graph.Edge edge = graph.addEdge(node1, node2);
            edge.setParam("color", "red");
        }
        return graph;
    }

}
