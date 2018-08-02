import com.google.refine.model.metadata.ProjectMetadata;
import com.google.refine.model.Project;

public class ProjectFake extends Project{

	@Override
	public ProjectMetadata getMetadata() {
		return new ProjectMetadata();
	}
}
