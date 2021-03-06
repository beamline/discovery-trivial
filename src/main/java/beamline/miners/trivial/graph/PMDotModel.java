package beamline.miners.trivial.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.apache.commons.lang3.tuple.Pair;

import beamline.graphviz.Dot;
import beamline.graphviz.DotNode;
import beamline.miners.trivial.ProcessMap;

/**
 *
 * @author Andrea Burattin
 */
public class PMDotModel extends Dot {

	private ProcessMap model;
	private ColorPalette.Colors activityColor;

	public PMDotModel(ProcessMap model, ColorPalette.Colors activityColor) {
		this.model = model;
		this.activityColor = activityColor;

		realize();
	}

	private void realize() {
//		setOption("rankdir", "LR");
		setOption("ranksep", ".1");
		setOption("fontsize", "9");
		setOption("remincross", "true");
		setOption("margin", "0.0,0.0");
		setOption("outputorder", "edgesfirst");

		Map<String, DotNode> activityToNode = new HashMap<>();
		Map<String, String> nodeToActivity = new HashMap<>();

		Set<DotNode> startNodes = new HashSet<>();
		Set<DotNode> endNodes = new HashSet<>();

		// add all activities
		for(String activity : model.getActivities()) {
			DotNode node = addNodeIfNeeded(activity, activityToNode, nodeToActivity);
			if (node instanceof PMDotNode) {
				((PMDotNode) node).setColorWeight(model.getActivityRelativeFrequency(activity), activityColor);
			}
			if (model.isStartActivity(activity)) {
				startNodes.add(node);
			}
			if (model.isEndActivity(activity)) {
				endNodes.add(node);
			}
		}

		// add all relations
		for (Pair<String, String> relation : model.getRelations()) {
			String sourceActivity = relation.getLeft();
			String targetActivity = relation.getRight();

			// adding source nodes
			DotNode sourceNode = addNodeIfNeeded(sourceActivity, activityToNode, nodeToActivity);
			// adding target nodes
			DotNode targetNode = addNodeIfNeeded(targetActivity, activityToNode, nodeToActivity);

			// adding relations
			addRelation(sourceNode, targetNode, model.getRelationRelativeValue(relation), model.getRelationAbsoluteValue(relation));
		}

		// add relations from start and end
		if (!startNodes.isEmpty()) {
			PMDotStartNode start = new PMDotStartNode();
			addNode(start);
			for (DotNode n : startNodes) {
				addRelation(start, n, null, null);
			}
		}
		if (!endNodes.isEmpty()) {
			PMDotEndNode end = new PMDotEndNode();
			addNode(end);
			for (DotNode n : endNodes) {
				addRelation(n, end, null, null);
			}
		}
	}

	private void addRelation(DotNode sourceNode, DotNode targetNode, Double relativeFrequency, Double absoluteFrequency) {
		String freqLabel = "";
		if (relativeFrequency != null && absoluteFrequency != null) {
			freqLabel = String.format("%.2g ", relativeFrequency) + "(" + absoluteFrequency.intValue() + ")";
		}
		addEdge(new PMDotEdge(sourceNode, targetNode, freqLabel, relativeFrequency));
	}

	private DotNode addNodeIfNeeded(String activity, Map<String, DotNode> activityToNode, Map<String, String> nodeToActivity) {
		DotNode existingNode = activityToNode.get(activity);
		if (existingNode == null) {
//			if (model.isStartActivity(activity)) {
//				PMCEPDotStartNode startNode = new PMCEPDotStartNode();
//				addNode(startNode);
//				activityToNode.put(activity, startNode);
//				nodeToActivity.put(startNode.getId(), activity);
//				return startNode;
//			} else if (model.isEndActivity(activity)) {
//				PMCEPDotEndNode endNode = new PMCEPDotEndNode();
//				addNode(endNode);
//				activityToNode.put(activity, endNode);
//				nodeToActivity.put(endNode.getId(), activity);
//				return endNode;
//			} else {
				PMDotNode newNode = new PMDotNode(activity.toString());
				newNode.setColorWeight(model.getActivityRelativeFrequency(activity), activityColor);
				newNode.setSecondLine(String.format("%.2g%n", model.getActivityRelativeFrequency(activity)) + " (" + model.getActivityAbsoluteFrequency(activity).intValue() + ")");
				addNode(newNode);
				activityToNode.put(activity, newNode);
				nodeToActivity.put(newNode.getId(), activity);
				return newNode;
//			}
		} else {
			return existingNode;
		}
	}
}