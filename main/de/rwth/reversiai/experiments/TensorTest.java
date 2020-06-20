package de.rwth.reversiai.experiments;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Random;

class TensorTest
{
    static
    {
        System.loadLibrary( "reversitensor" );
    }

    private int width;
    private int height;
    private int depth;

    private byte[][][] multiDimArray;
    private byte[] flatArray;
    private long[] packedArray;
    private long unsafeArray;

    private static final byte fieldSize = 6;
    private static final byte fieldsPerLong = 64 / fieldSize;
    private static final long bitmask = 0b00111111;

    private Unsafe getUnsafe()
    {
        try
        {
            Field f = Unsafe.class.getDeclaredField( "theUnsafe" );

            f.setAccessible( true );

            return (Unsafe) f.get( null );
        }
        catch ( Exception e )
        {

        }
        return null;
    }

    public TensorTest( int width, int height, int depth )
    {
        this.width = width;
        this.height = height;
        this.depth = depth;

        int length = width * height * depth;

        this.multiDimArray = new byte[ width ][ height ][ depth ];
        this.flatArray = new byte[ length ];
        this.packedArray = new long[ length / fieldsPerLong + 1 ];
        unsafeArray = getUnsafe().allocateMemory( length );

        initNativeFlatArray( width, height, depth );
    }

    public byte getUnsafeArray( int i, int j, int k )
    {
        return getUnsafe().getByte( unsafeArray + getLinearIndex( i, j, k ) );
    }

    public void setUnsafeArray( int i, int j, int k, byte value )
    {
        getUnsafe().putByte( unsafeArray + getLinearIndex( i, j, k ), value );
    }

    private static native void initNativeFlatArray( int width, int height, int depth );

    public static native byte getNativeFlatArray( int x, int y, int z );

    public static native void setNativeFlatArray( int x, int y, int z, byte value );

    private int getLinearIndex( int x, int y, int z )
    {
        return z + y * depth + x * depth * height;
    }

    public byte get3DArray( int x, int y, int z )
    {
        return multiDimArray[ x ][ y ][ z ];
    }

    public void set3DArray( int x, int y, int z, byte value )
    {
        multiDimArray[ x ][ y ][ z ] = value;
    }

    public byte getFlatArray( int x, int y, int z )
    {
        return flatArray[ getLinearIndex( x, y, z ) ];
    }

    public void setFlatArray( int x, int y, int z, byte value )
    {
        flatArray[ getLinearIndex( x, y, z ) ] = value;
    }

    public byte getPackedArray( int x, int y, int z )
    {
        int index = getLinearIndex( x, y, z );

        int longIndex = index / fieldsPerLong;
        int longOffset = fieldSize * ( index % fieldsPerLong );

        return (byte) ( ( packedArray[ longIndex ] & ( bitmask << longOffset ) ) >> longOffset );
    }

    public void setPackedArray( int x, int y, int z, byte value )
    {
        int index = getLinearIndex( x, y, z );

        int longIndex = index / fieldsPerLong;
        int longOffset = fieldSize * ( index % fieldsPerLong );

        long deleteMask = ~( bitmask << longOffset );
        long writeMask = ( bitmask & value ) << longOffset;

        packedArray[ longIndex ] = ( packedArray[ longIndex ] & deleteMask ) | writeMask;
    }

    public void testLinearIndexing()
    {
        int i = 0;

        for ( int x = 0; x < 50; x++ )
        {
            for ( int y = 0; y < 50; y++ )
            {
                for ( int z = 0; z < 20; z++ )
                {
                    assert i == getLinearIndex( x, y, z );

                    i++;
                }
            }
        }
    }

    public void testIndexing()
    {

    }

    public long[] timeOrdered()
    {
        long[] results = new long[ 10 ];

        long tic = System.nanoTime();

        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    byte write = (byte) ( x + y + z );

                    set3DArray( x, y, z, write );
                }
            }
        }

        long toc = System.nanoTime();

        results[ 0 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    byte write = (byte) ( x + y + z );

                    setFlatArray( x, y, z, write );
                }
            }
        }

        toc = System.nanoTime();

        results[ 1 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    byte write = (byte) ( x + y + z );

                    setPackedArray( x, y, z, write );
                }
            }
        }

        toc = System.nanoTime();

        results[ 2 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    byte write = (byte) ( x + y + z );

                    setNativeFlatArray( x, y, z, write );
                }
            }
        }

        toc = System.nanoTime();

        results[ 3 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    byte write = (byte) ( x + y + z );

                    setUnsafeArray( x, y, z, write );
                }
            }
        }

        toc = System.nanoTime();

        results[ 4 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    get3DArray( x, y, z );
                }
            }
        }

        toc = System.nanoTime();

        results[ 5 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    getFlatArray( x, y, z );
                }
            }
        }

        toc = System.nanoTime();

        results[ 6 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    getPackedArray( x, y, z );
                }
            }
        }

        toc = System.nanoTime();

        results[ 7 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    getNativeFlatArray( x, y, z );
                }
            }
        }

        toc = System.nanoTime();

        results[ 8 ] = toc - tic;

        tic = System.nanoTime();

        for ( int y = 0; y < height; ++y )
        {
            for ( int x = 0; x < width; ++x )
            {
                for ( int z = 0; z < depth; ++z )
                {
                    getUnsafeArray( x, y, z );
                }
            }
        }

        toc = System.nanoTime();

        results[ 9 ] = toc - tic;

        return results;
    }

    public long[] timeRandom()
    {
        long[] results = new long[ 10 ];

        Random rand = new Random();

        long tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            byte write = (byte) ( x + y + z );

            set3DArray( x, y, z, write );
        }

        long toc = System.nanoTime();

        results[ 0 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            byte write = (byte) ( x + y + z );

            setFlatArray( x, y, z, write );
        }

        toc = System.nanoTime();

        results[ 1 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            byte write = (byte) ( x + y + z );

            setPackedArray( x, y, z, write );
        }

        toc = System.nanoTime();

        results[ 2 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            byte write = (byte) ( x + y + z );

            setNativeFlatArray( x, y, z, write );
        }

        toc = System.nanoTime();

        results[ 3 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            byte write = (byte) ( x + y + z );

            setUnsafeArray( x, y, z, write );
        }

        toc = System.nanoTime();

        results[ 4 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            get3DArray( x, y, z );
        }

        toc = System.nanoTime();

        results[ 5 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            getFlatArray( x, y, z );
        }

        toc = System.nanoTime();

        results[ 6 ] = toc - tic;

        tic = System.nanoTime();

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            getPackedArray( x, y, z );
        }

        toc = System.nanoTime();

        results[ 7 ] = toc - tic;

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            getNativeFlatArray( x, y, z );
        }

        toc = System.nanoTime();

        results[ 8 ] = toc - tic;

        for ( int i = 0; i < width * height * depth; i++ )
        {
            int x = rand.nextInt( width );
            int y = rand.nextInt( height );
            int z = rand.nextInt( depth );

            getUnsafeArray( x, y, z );
        }

        toc = System.nanoTime();

        results[ 9 ] = toc - tic;

        return results;
    }

    private long[] avgOrdered( int times )
    {
        long[] result = timeOrdered();

        for ( int i = 0; i < times - 1; i++ )
        {
            long[] temp = timeOrdered();

            for ( int j = 0; j < temp.length; j++ )
            {
                result[ j ] += temp[ j ];
            }
        }

        for ( int j = 0; j < result.length; j++ )
        {
            result[ j ] /= (double) times;
        }

        return result;
    }

    private long[] avgRandom( int times )
    {
        long[] result = timeRandom();

        for ( int i = 0; i < times - 1; i++ )
        {
            long[] temp = timeRandom();

            for ( int j = 0; j < temp.length; j++ )
            {
                result[ j ] += temp[ j ];
            }
        }

        for ( int j = 0; j < result.length; j++ )
        {
            result[ j ] /= (double) times;
        }

        return result;
    }

    public static void main( String[] args )
    {
        TensorTest tensor = new TensorTest( 50, 50, 20 );

        System.out.println( "Check correctness of linear indexing ... \n" );
        tensor.testLinearIndexing();

        System.out.println( "Time ordered access ... \n" );

        // Warm up JVM (JIT compiler)
        for ( int i = 0; i < 50; i++ )
            tensor.timeOrdered();

        long[] result1 = tensor.avgOrdered( 1000 );

        System.out.println( "Write" );
        System.out.println( "------------" );
        System.out.println( "3D array:          " + result1[ 0 ] + " ns" );
        System.out.println( "Flat array:        " + result1[ 1 ] + " ns" );
        System.out.println( "Packed array:      " + result1[ 2 ] + " ns" );
        System.out.println( "Native flat array: " + result1[ 3 ] + " ns" );
        System.out.println( "Unsafe flat array: " + result1[ 4 ] + " ns\n" );
        System.out.println( "Read" );
        System.out.println( "------------" );
        System.out.println( "3D array:          " + result1[ 5 ] + " ns" );
        System.out.println( "Flat array:        " + result1[ 6 ] + " ns" );
        System.out.println( "Packed array:      " + result1[ 7 ] + " ns" );
        System.out.println( "Native flat array: " + result1[ 8 ] + " ns" );
        System.out.println( "Unsafe flat array: " + result1[ 9 ] + " ns \n" );

        System.out.println( "Time random access ... \n" );

        for ( int i = 0; i < 50; i++ )
            tensor.timeRandom();

        long[] result2 = tensor.avgRandom( 1000 );

        System.out.println( "Write" );
        System.out.println( "------------" );
        System.out.println( "3D array:          " + result2[ 0 ] + " ns" );
        System.out.println( "Flat array:        " + result2[ 1 ] + " ns" );
        System.out.println( "Packed array:      " + result2[ 2 ] + " ns" );
        System.out.println( "Native flat array: " + result2[ 3 ] + " ns" );
        System.out.println( "Unsafe flat array: " + result2[ 4 ] + " ns\n" );
        System.out.println( "Read" );
        System.out.println( "------------" );
        System.out.println( "3D array:          " + result2[ 5 ] + " ns" );
        System.out.println( "Flat array:        " + result2[ 6 ] + " ns" );
        System.out.println( "Packed array:      " + result2[ 7 ] + " ns" );
        System.out.println( "Native flat array: " + result2[ 8 ] + " ns" );
        System.out.println( "Unsafe flat array: " + result2[ 9 ] + " ns \n" );
    }
}