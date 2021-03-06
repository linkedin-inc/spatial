package org.neo4j.gis.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import org.neo4j.gis.spatial.pipes.processing.OrthodromicDistance;
import org.neo4j.gis.spatial.rtree.Envelope;
import org.neo4j.gis.spatial.rtree.EnvelopeDecoderFromDoubleArray;
import org.neo4j.gis.spatial.rtree.RTreeIndex;
import org.neo4j.gis.spatial.rtree.RTreeRelationshipTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;

/**
 * Created by yihe on 6/21/15.
 */

public class SingleNode implements Comparable<SingleNode>{
    public Node node;
    public boolean knnItem;

    Coordinate core;

    Envelope envelope;

    EnvelopeDecoderFromDoubleArray child_encoder = new EnvelopeDecoderFromDoubleArray(RTreeIndex.INDEX_PROP_BBOX);
    EnvelopeDecoderFromDoubleArray leaf_encoder = new EnvelopeDecoderFromDoubleArray(Constants.PROP_BBOX);

    double distance;


    public SingleNode(Node node, Coordinate core){
        this.node = node;
        this.core = core;

        if(isLeafNode(node)) {
            this.envelope = leaf_encoder.decodeEnvelope(node);
            this.knnItem = true;
        }else {
            this.envelope = child_encoder.decodeEnvelope(node);
            this.knnItem = false;
        }
        distance = boxDist();
    }

    public double boxDist(){
        double dx = axisDist(core.x, envelope.getMinX(), envelope.getMaxX());
        double dy = axisDist(core.y, envelope.getMinY(), envelope.getMaxY());

        return OrthodromicDistance.calculateDistance(core, new Coordinate(dx, dy));
    }

    private double axisDist(double k, double min, double max){
        return k < min ? min:
                k <= max ? k :
                        max;
    }

    private boolean isLeafNode(Node node){
        return !(node.hasRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING) ||
                node.hasRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING) );
    }

    @Override
    public int compareTo(SingleNode b) {
        return (int)(distance - b.getDistance());
    }

    public Node getNode() {
        return node;
    }

    public boolean isKnnItem() {
        return knnItem;
    }

    public double getDistance() {
        return distance;
    }
}