package data_representation;

/**
 * @author miriamhuijser
 * Class MatrixIndices saves a matrix index in its row and column.
 */
public class MatrixIndices{
	public int row;
	public int column;
	
	/**
	 * Constructor
	 * @param row - row of index
	 * @param column - column of index
	 */
	public MatrixIndices( int row, int column ){
		this.row = row;
		this.column = column;
	}
}