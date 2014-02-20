package jahmm.jadetree;

import jahmm.jadetree.abstracts.DecisionInode;
import jahmm.jadetree.abstracts.DecisionNode;
import jahmm.jadetree.abstracts.DecisionRealNode;
import jahmm.jadetree.abstracts.DecisionTree;
import jahmm.jadetree.objectattributes.ObjectAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jutils.designpatterns.CompositeLeaf;
import jutils.iterators.SingleIterable;
import jutlis.tuples.HolderBase;

/**
 *
 * @author kommusoft
 * @param <TSource>
 */
public final class DecisionLeafImpl<TSource> extends DecisionNodeBase<TSource> implements CompositeLeaf<DecisionNode<TSource>> {

    private final List<TSource> memory;
    private double score = Double.NaN;
    private int splitIndex = 0x00;
    private final HolderBase<Object> splitData = new HolderBase<>();

    public DecisionLeafImpl(DecisionInode<TSource> parent) {
        this(parent, new ArrayList<TSource>());
    }

    public DecisionLeafImpl(DecisionInode<TSource> parent, List<TSource> memory) {
        super(parent);
        this.memory = memory;
    }

    public DecisionLeafImpl(DecisionInode<TSource> parent, Iterable<TSource> elements) {
        this(parent);
        for (TSource element : elements) {
            this.insert(element);
        }
    }

    public boolean isDirty() {
        return Double.isNaN(this.score);
    }

    @Override
    public void makeDirty() {
        this.score = Double.NaN;
        splitData.setData(null);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public double expandScore() {
        if (this.isDirty()) {
            this.score = this.calculateScore();
        }
        return this.score;
    }

    @Override
    public DecisionRealNode<TSource> expand() {
        DecisionTree<TSource> tree = this.getTree();
        this.expandScore();
        DecisionRealNode<TSource> foo = tree.getSourceAttributes().get(splitIndex).createDecisionNode(this.getParent(), this.memory, tree.getTargetAttribute(), this.splitData);
        this.getParent().replaceChild(this, foo);
        return foo;
    }

    @Override
    public void insert(TSource source) {
        this.makeDirty();
        memory.add(source);
    }

    double calculateScore() {
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxIndex = 0x00, i = 0x00;
        HolderBase<Object> curData = new HolderBase<>();
        ObjectAttribute<TSource, Object> target = this.getTree().getTargetAttribute();
        for (ObjectAttribute<TSource, ?> oa : this.getTree().getSourceAttributes()) {
            double sc = oa.calculateScore(this.memory, target, curData);
            if (sc > maxScore) {
                maxScore = sc;
                maxIndex = i;
                this.splitData.copyFrom(curData);
            }
            i++;
        }
        this.splitIndex = maxIndex;
        return maxScore;
    }

    @Override
    public DecisionLeafImpl<TSource> getMaximumExpandLeaf() {
        return this;
    }

    @Override
    public void replaceChild(DecisionRealNode<TSource> was, DecisionRealNode<TSource> now) {
    }

    @Override
    public double reduceScore() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public List<TSource> getStoredSources() {
        return Collections.unmodifiableList(this.memory);
    }

    @Override
    public Iterable<Iterable<TSource>> getPartitionedStoredSources() {
        return new SingleIterable<Iterable<TSource>>(this.getStoredSources());
    }

    @Override
    public DecisionRealNode<TSource> reduceThis() {
        return this;
    }

    @Override
    public DecisionInode<TSource> getMaximumReduceInode() {
        return this.getParent();
    }

    @Override
    public DecisionRealNode<TSource> reduce() {
        //TODO: move to the parent
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
