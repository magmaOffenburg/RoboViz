package rv.comm.rcssserver.scenegraph;

import java.util.ArrayList;
import rv.comm.rcssserver.SExp;

/**
 * Contains metadata/descriptions about its children.
 *
 * @author Hannes Braun
 */
public class DescriptionNode extends Node
{
	/** Abbreviation declaring this node type in an s-expression */
	public static final String EXP_ABRV = "DSC";

	private final ArrayList<String[]> descriptions;

	public DescriptionNode(Node parent, SExp exp)
	{
		super(parent);
		descriptions = new ArrayList<>(exp.getChildren().size());
		for (var child : exp.getChildren()) {
			descriptions.add(child.getAtoms());
		}
	}

	public ArrayList<String[]> getDescriptions()
	{
		return descriptions;
	}
}
