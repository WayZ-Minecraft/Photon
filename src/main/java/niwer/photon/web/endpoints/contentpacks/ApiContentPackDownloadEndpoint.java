package niwer.photon.web.endpoints.contentpacks;

public class ApiContentPackDownloadEndpoint extends ContentPackDownloadEndpoint {

	@Override public String path() { return "/api/downloads/{packId}"; }
}