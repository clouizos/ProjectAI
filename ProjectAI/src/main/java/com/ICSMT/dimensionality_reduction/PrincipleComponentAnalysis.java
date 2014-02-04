package dimensionality_reduction;

import io.IOFile;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.SingularOps;
import data_representation.FeatureMatrix;

public class PrincipleComponentAnalysis {

    // principle component subspace is stored in the rows
    private DenseMatrix64F V_t;

    // how many principle components are used
    private int numComponents;

    // where the data is stored
    private DenseMatrix64F A = new DenseMatrix64F(1,1);
    private int sampleIndex;

    // mean values of each element across all the samples
    double mean[];

    public PrincipleComponentAnalysis() {
    }

    /**
     * Must be called before any other functions. Declares and sets up internal data structures.
     *
     * @param numSamples Number of samples that will be processed.
     * @param sampleSize Number of elements in each sample.
     */
    public void setup( int numSamples , int sampleSize ) {
        mean = new double[ sampleSize ];
        A.reshape(numSamples,sampleSize,false);
        sampleIndex = 0;
        numComponents = -1;
    }

    /**
     * Adds a new sample of the raw data to internal data structure for later processing.  All the samples
     * must be added before computeBasis is called.
     *
     * @param sampleData Sample from original raw data.
     */
    public void addSample( double[] sampleData ) {
        if( A.getNumCols() != sampleData.length )
            throw new IllegalArgumentException("Unexpected sample size");
        if( sampleIndex >= A.getNumRows() )
            throw new IllegalArgumentException("Too many samples");

        for( int i = 0; i < sampleData.length; i++ ) {
            A.set(sampleIndex,i,sampleData[i]);
        }
        sampleIndex++;
    }

    /**
     * Computes a basis (the principle components) from the most dominant eigenvectors.
     *
     * @param numComponents Number of vectors it will use to describe the data.  Typically much
     * smaller than the number of elements in the input vector.
     */
    public void computeBasis( int numComponents ) {
        if( numComponents > A.getNumCols() )
            throw new IllegalArgumentException("More components requested that the data's length.");
        if( sampleIndex != A.getNumRows() )
            throw new IllegalArgumentException("Not all the data has been added");
        //if( numComponents > sampleIndex )
        //    throw new IllegalArgumentException("More data needed to compute the desired number of components");

        this.numComponents = numComponents;

        // compute the mean of all the samples
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                mean[j] += A.get(i,j);
            }
        }
        for( int j = 0; j < mean.length; j++ ) {
            mean[j] /= A.getNumRows();
        }

        // subtract the mean from the original data
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                A.set(i,j,A.get(i,j)-mean[j]);
            }
        }

        // Compute SVD and save time by not computing U
        SingularValueDecomposition<DenseMatrix64F> svd =
                DecompositionFactory.svd(A.numRows, A.numCols, true, true, true);
        if( !svd.decompose(A) )
            throw new RuntimeException("SVD failed");

        V_t = svd.getU(null,false);
        DenseMatrix64F W = svd.getW(null);
        

        // Singular values are in an arbitrary order initially
        SingularOps.descendingOrder(V_t,false,W,null,true);

        // strip off unneeded components and find the basis
        V_t.reshape(V_t.numRows,numComponents,true);
    }

    /**
     * Returns a vector from the PCA's basis.
     *
     * @param which Which component's vector is to be returned.
     * @return Vector from the PCA basis.
     */
    public double[] getBasisVector( int which ) {
        if( which < 0 || which >= numComponents )
            throw new IllegalArgumentException("Invalid component");

        DenseMatrix64F v = new DenseMatrix64F(A.numRows,1);
        CommonOps.extract(V_t,0,A.numRows,which,which+1,v,0,0);

        return v.data;
    }

    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    public double[] sampleToEigenSpace( double[] sampleData ) {
        if( sampleData.length != A.getNumCols() )
            throw new IllegalArgumentException("Unexpected sample length");
        DenseMatrix64F mean = DenseMatrix64F.wrap(A.getNumCols(),1,this.mean);

        DenseMatrix64F s = new DenseMatrix64F(A.getNumCols(),1,true,sampleData);
        DenseMatrix64F r = new DenseMatrix64F(numComponents,1);
        
        CommonOps.sub(s,mean,s);

        CommonOps.mult(V_t,s,r);

        return r.data;
    }

    /**
     * Converts a vector from eigen space into sample space.
     *
     * @param eigenData Eigen space data.
     * @return Sample space projection.
     */
    public double[] eigenToSampleSpace( double[] eigenData ) {
        if( eigenData.length != numComponents )
            throw new IllegalArgumentException("Unexpected sample length");

        DenseMatrix64F s = new DenseMatrix64F(A.getNumCols(),1);
        DenseMatrix64F r = DenseMatrix64F.wrap(numComponents,1,eigenData);
        
        CommonOps.multTransA(V_t,r,s);

        DenseMatrix64F mean = DenseMatrix64F.wrap(A.getNumCols(),1,this.mean);
        CommonOps.add(s,mean,s);

        return s.data;
    }


    /**
     * <p>
     * The membership error for a sample.  If the error is less than a threshold then
     * it can be considered a member.  The threshold's value depends on the data set.
     * </p>
     * <p>
     * The error is computed by projecting the sample into eigenspace then projecting
     * it back into sample space and
     * </p>
     * 
     * @param sampleA The sample whose membership status is being considered.
     * @return Its membership error.
     */
    public double errorMembership( double[] sampleA ) {
        double[] eig = sampleToEigenSpace(sampleA);
        double[] reproj = eigenToSampleSpace(eig);


        double total = 0;
        for( int i = 0; i < reproj.length; i++ ) {
            double d = sampleA[i] - reproj[i];
            total += d*d;
        }

        return Math.sqrt(total);
    }

    /**
     * Computes the dot product of each basis vector against the sample.  Can be used as a measure
     * for membership in the training sample set.  High values correspond to a better fit.
     *
     * @param sample Sample of original data.
     * @return Higher value indicates it is more likely to be a member of input dataset.
     */
    public double response( double[] sample ) {
        if( sample.length != A.numCols )
            throw new IllegalArgumentException("Expected input vector to be in sample space");

        DenseMatrix64F dots = new DenseMatrix64F(numComponents,1);
        DenseMatrix64F s = DenseMatrix64F.wrap(A.numCols,1,sample);

        CommonOps.mult(V_t,s,dots);

        return NormOps.normF(dots);
    }
    
    public static void main(String[] arg) {
		System.out.println(System.getProperty("user.dir"));
		String filePath = "../Testdata/dataset/English";
		int nPC = 10;
		int nDocs = -1;
		FeatureMatrix FM = new FeatureMatrix(filePath,nDocs,"english","prob");
		System.out.println(FM.matrix[0].length);
		
		PrincipleComponentAnalysis Pca = new PrincipleComponentAnalysis();
		Pca.setup(FM.rowLabel.size(), FM.columnLabel.size());
		for (int i=0;i<FM.rowLabel.size();i++){
			Pca.addSample(FM.matrix[i]);
		}
		Pca.computeBasis(nPC);
		System.out.println(Pca.V_t.getNumRows());
		System.out.println(Pca.V_t.getNumCols());
		IOFile IO = new IOFile();
		String fileName = "PCAFeatureVector-"+Pca.V_t.getNumRows()+"-"+Pca.V_t.getNumCols()+".data";
		IO.createWriteFile(fileName);
		for (int i=0;i<Pca.V_t.getNumRows();i++){
			IO.write(FM.rowLabel.get(i)+",");
			for (int j=0;j<nPC;j++){
			//	System.out.printf("%.5f \t",Pca.V_t.get(i, j));
				IO.write(Double.toString(Pca.V_t.get(i,j)));
				IO.write(" ");
			}
			IO.write("\n");
		}
		IO.close();
		
		 
	}
}
