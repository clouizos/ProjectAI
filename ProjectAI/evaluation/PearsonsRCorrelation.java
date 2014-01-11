package evaluation;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;

/**
 * 
 * @author miriamhuijser
 * Class PearsonsRCorrelation provides methods to compute the correlation
 * between two files with a matrix of values. 
 */
public class PearsonsRCorrelation{
	String fileName1;
	String fileName2;
	ArrayList<Double> matrix1 = new ArrayList<Double>();
	ArrayList<Double> matrix2 = new ArrayList<Double>();

	public PearsonsRCorrelation(String fileName1, String fileName2){
		this.fileName1 = fileName1;
		this.fileName2 = fileName2;
	}

	/**
	 * This method parses the two files with the matrices and saves the values
	 * in two lists.
	 */
	public void makeMatrices(){
		File file1, file2;
		Scanner s1, s2;
		try{
			file1 = new File(fileName1);
			file2 = new File(fileName2);
			s1 = new Scanner(file1);
			s2 = new Scanner(file2);
			s1.useDelimiter(",|\n");
			s2.useDelimiter(",|\n");
			while(s1.hasNext()){
				String value = s1.next();
				if( !value.isEmpty() ){
					Double nValue = Double.parseDouble(value);
					matrix1.add(nValue);
				}
			}
			while(s2.hasNext()){
				String value = s2.next();
				if( !value.isEmpty() ){
					Double nValue = Double.parseDouble(value);
					matrix2.add(nValue);
				}
			}
		}catch( IOException e){System.out.println(e.getMessage());}		
	}

	/**
	 * This method computes the correlation between the two matrices
	 * @return r - correlation
	 */
	public double computeCorrelation(){
		double meanX = 0;
		double meanY = 0;
		double a = 0;
		double b1 = 0;
		double b2 = 0;
		double b;
		double r;
		for( int i = 0; i < matrix1.size(); i++ ){
			meanX = meanX + matrix1.get(i);
			meanY = meanY + matrix2.get(i);
		}
		meanX = meanX / (double) matrix1.size();
		meanY = meanY / (double) matrix2.size();

		for( int i = 0; i < matrix1.size(); i++){
			double xi = matrix1.get(i);
			double yi = matrix2.get(i);
			double a1 = xi - meanX;
			double a2 = yi - meanY;
			double a3 = a1 * a2;
			a = a + a3;

			b1 = b1 + Math.pow(a1, 2);
			b2 = b2 + Math.pow(a2, 2);
		}

		b1 = Math.sqrt(b1);
		b2 = Math.sqrt(b2);
		b = b1 * b2;
		r = a / b;

		return r;
	}
}