package jadetree.objectattributes;

/**
 *
 * @author kommusoft
 * @param <TSource>
 */
public abstract class DoubleContinuObjectAttribute<TSource> extends ContinuObjectAttributeBase<TSource, Double> {

    @Override
    public Double getBetween(TSource source1, TSource source2) {
        return 0.5d * (this.evaluate(source1) + this.evaluate(source2));
    }

    @Override
    public int compareWith(TSource source, Double target) {
        return this.evaluate(source).compareTo(target);
    }

    @Override
    public int compare(TSource source1, TSource source2) {
        return this.evaluate(source1).compareTo(this.evaluate(source2));
    }

}