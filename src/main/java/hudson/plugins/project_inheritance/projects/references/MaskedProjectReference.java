
package hudson.plugins.project_inheritance.projects.references;

import hudson.Extension;
import hudson.RelativePath;
import hudson.plugins.project_inheritance.projects.InheritanceProject;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 *
 * @author sam.oates
 */
public class MaskedProjectReference extends SimpleProjectReference {

	@DataBoundConstructor
	public MaskedProjectReference(String targetJob) {
		super(targetJob);
	}
	
	@Override
	public InheritanceProject getProject(InheritanceProject owner) {
		
		Jenkins j = Jenkins.getActiveInstance();
		String resolvedName = ResolveMaskName(this.name, owner.getFullName());
		InheritanceProject proj = (InheritanceProject)j.getItemByFullName(resolvedName);
		return proj;
	}
		
	public static String ResolveMaskName(String mask, String localName)
	{
		String resolvedJob = "";
			
		String[] localParts = localName.split("/");
		String[] maskedParts = mask.split("/");

		for (int partIndex = 0; partIndex < maskedParts.length; ++partIndex)
		{
			String maskPart = maskedParts[partIndex];

			if ("*".equals(maskPart))
			{
				// TODO: bounds check
				String localPart = localParts[partIndex];
				resolvedJob = resolvedJob + localPart;
			}
			else
			{
				resolvedJob = resolvedJob + maskPart;
			}

			if (partIndex < maskedParts.length - 1)
			{
				resolvedJob += "/";
			}
		}
			
		return resolvedJob;
	}
	
	@Extension
	public static class MaskedProjectReferenceDescriptor extends SimpleProjectReferenceDescriptor {
		@Override
		public String getDisplayName() {
			return Messages.MaskedProjectReference_DisplayName();
		}
		
		@Override
		public FormValidation doCheckName(
			@QueryParameter String targetJob,
			@RelativePath(value="..") @QueryParameter String parents,
			@AncestorInPath InheritanceProject localJob) {

			String resolvedJob = MaskedProjectReference.ResolveMaskName(targetJob, localJob.getFullName());
			return super.doCheckName(resolvedJob, parents, localJob);
		}
	}
}