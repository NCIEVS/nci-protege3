package com.clarkparsia.dig20.server.admin;

import java.util.Properties;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Oct 28, 2009
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public class InfoResponse extends Properties implements AdminResponse {	
	public void accept(AdminResponseVisitor v) {
		v.visit(this);
	}
}
