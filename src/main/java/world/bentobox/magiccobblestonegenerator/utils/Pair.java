package world.bentobox.magiccobblestonegenerator.utils;


/**
 * Class to store pairs of objects, e.g. coordinates
 *
 * @param <X> the x part of the pair
 * @param <Z> the z part of the pair
 * @author tastybento, BONNe
 */
public class Pair<X, Z>
{
    public Pair(X first, Z second)
    {
        this.first = first;
        this.second = second;
    }


    /**
     * Gets first object.
     *
     * @return the first
     */
    public X getKey()
    {
        return first;
    }


    /**
     * Sets first.
     *
     * @param first the first
     */
    public void setKey(X first)
    {
        this.first = first;
    }


    /**
     * Gets second object.
     *
     * @return the second
     */
    public Z getValue()
    {
        return second;
    }


    /**
     * Sets second.
     *
     * @param second the second
     */
    public void setValue(Z second)
    {
        this.second = second;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Pair [x=" + first + ", z=" + second + "]";
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (!(obj instanceof Pair<?, ?> other))
        {
            return false;
        }

        if (first == null)
        {
            if (other.first != null)
            {
                return false;
            }
        }
        else if (!first.equals(other.first))
        {
            return false;
        }

        if (second == null)
        {
            return other.second == null;
        }
        else
        {
            return second.equals(other.second);
        }
    }


    private X first;

    private Z second;
}