 /*
  * Contributor(s): Prashanth Ranganathan prashr@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.users;


public class ChangeStats {
    private int createCount;
    private int modifyCount;
    private int deleteCount;

    public int getCreateCount() {
        return createCount;
    }

    public void setCreateCount(int createCount) {
        this.createCount = createCount;
    }

    public int getModifyCount() {
        return modifyCount;
    }

    public void setModifyCount(int modifyCount) {
        this.modifyCount = modifyCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }
}
