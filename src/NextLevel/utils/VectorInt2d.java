package NextLevel.utils;

import tools.Vector2d;

public class VectorInt2d {
	
	public int x;
	public int y;
	
	public VectorInt2d (int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public VectorInt2d (Vector2d v)
	{
		this.x = (int) Math.round(v.x);
		this.y = (int) Math.round(v.y);
	}
	
	public VectorInt2d add (int x, int y)
	{
		return new VectorInt2d(this.x + x, this.y + y);
	}
	
    @Override
	public boolean equals(Object o)
    {
        if (o instanceof VectorInt2d) {
            VectorInt2d v = (VectorInt2d) o;
            return x == v.x && y == v.y;
        } else {
            return false;
        }
    }
	
    @Override
	public int hashCode()
    {
        return 1024*x + y;
    }
    
    @Override
	public String toString() {
        return x + " : " + y;
    }
}
