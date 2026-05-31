package rv.comm.rcssserver;

import java.util.List;

public record ProtocolVersion(int major, int minor)
{
	public ProtocolVersion(List<String> version)
	{
		this(Integer.parseInt(version.get(0)), Integer.parseInt(version.get(1)));
	}

	public boolean supports(int major, int minor)
	{
		return major == this.major && minor >= this.minor;
	}

	@Override
	public String toString()
	{
		return major + "." + minor;
	}
}
