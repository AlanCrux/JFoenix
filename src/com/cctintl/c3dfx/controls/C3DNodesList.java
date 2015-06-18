package com.cctintl.c3dfx.controls;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * @author sshahine
 * list of nodes that are toggled On/Off by clicking on the 1st node
 */

public class C3DNodesList extends VBox {
	
	HashMap<Node, Callback<Boolean, ArrayList<KeyValue>>> animationsMap = new HashMap<>();
	private boolean expanded = false;
	private Timeline animateTimeline = new Timeline();
	
	public C3DNodesList() {
		this.setPickOnBounds(false);
		this.getStyleClass().add("c3d-nodes-list");
	}
	
	public void addAnimatedNode(Region node){
		addAnimatedNode(node, null);
	}
	
	public void addAnimatedNode(Region node, Callback<Boolean, ArrayList<KeyValue>> animationCallBack ){
		// create container for the node if it's a sub nodes list
		if(node instanceof C3DNodesList){ 
			addAnimatedNode(new C3DNodesListContainer(node), animationCallBack);
			return;
		}
		
		// init node property
		node.setVisible(false);
		node.minWidthProperty().bind(node.prefWidthProperty());
		node.minHeightProperty().bind(node.prefHeightProperty());
		if(this.getChildren().size() > 0) initNode(node);
		else {
			if(node instanceof Button) ((Button)node).setOnAction((action)-> this.animateList());
			else node.setOnMouseClicked((click)-> this.animateList());
		}
		
		// init the list height and width
		if(this.getChildren().size() == 0 ){
			node.setVisible(true);
			this.minHeightProperty().bind(node.prefHeightProperty());
			this.maxHeightProperty().bind(node.prefHeightProperty());
			this.minWidthProperty().bind(node.prefHeightProperty());
			this.maxWidthProperty().bind(node.prefHeightProperty());
		}				
		
		// add the node and its listeners
		this.getChildren().add(node);
		this.rotateProperty().addListener((o,oldVal,newVal)-> node.setRotate(newVal.doubleValue() % 180 == 0 ? newVal.doubleValue() : -newVal.doubleValue()));	
		if(animationCallBack == null) animationCallBack = (expanded)-> {return initDefaultAnimation(node, expanded);};
		animationsMap.put(node, animationCallBack);
	}
	
	public void animateList(){
		expanded = !expanded;
		
		if(animateTimeline.getStatus().equals(Status.RUNNING)) animateTimeline.stop();
		
		animateTimeline.getKeyFrames().clear();
		double duration = 120/this.getChildren().size();

		// show child nodes 
		if(expanded) this.getChildren().forEach(child->child.setVisible(true));
		
		// add child nodes animation
		for(int i = 1; i < this.getChildren().size();i++){
			Node child = this.getChildren().get(i);
			ArrayList<KeyValue> keyValues = animationsMap.get(child).call(expanded);
			animateTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(i*duration), keyValues.toArray(new KeyValue[keyValues.size()])));
		}
		// add 1st element animation
		ArrayList<KeyValue> keyValues = animationsMap.get(this.getChildren().get(0)).call(expanded);
		animateTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(160), keyValues.toArray(new KeyValue[keyValues.size()])));
		
		// hide child nodes to allow mouse events on the nodes behind them
		if(!expanded) {
			animateTimeline.setOnFinished((finish)->{
				for(int i = 1; i < this.getChildren().size();i++) 
					this.getChildren().get(i).setVisible(false);
			});
		}else{
			animateTimeline.setOnFinished(null);
		}
		
		animateTimeline.play();
	}
	
	protected void initNode(Node node){
		node.setScaleX(0);
		node.setScaleY(0);
	}
	
	// init default animation keyvalues
	private ArrayList<KeyValue> initDefaultAnimation(Region region, boolean expanded) {
		return new ArrayList<KeyValue>(){{
			add(new KeyValue(region.scaleXProperty(), expanded?1:0 , Interpolator.EASE_BOTH));
			add(new KeyValue(region.scaleYProperty(), expanded?1:0, Interpolator.EASE_BOTH));
		}};
	}
	
}
