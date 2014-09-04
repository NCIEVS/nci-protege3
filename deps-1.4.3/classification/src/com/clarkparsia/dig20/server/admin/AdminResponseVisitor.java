package com.clarkparsia.dig20.server.admin;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Oct 28, 2009
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public interface AdminResponseVisitor {
	public void visit(InfoResponse infoResponse);
}
