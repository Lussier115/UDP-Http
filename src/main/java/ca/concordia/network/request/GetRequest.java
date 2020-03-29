package ca.concordia.network.request;

import ca.concordia.network.exception.InvalidRequestException;

public class GetRequest extends Request {

    public GetRequest(String[] args, RequestType requestType) throws InvalidRequestException {
        this.setRequestType(requestType);

        for (int i = 1; i < args.length; i++) {

            if (requestType == RequestType.GET) {
                if (args[i].equals("-d") || args[i].equals("-f")) {
                    throw new InvalidRequestException("Cannot contain -d or -f");
                }
            }
        }

        this.parseRequest(args);
    }

    @Override
    public String toString() {
        String request = requestType.toString() + " " + url + " " + version + "\r\n" + this.headers.toString() + "\r\n";

        return request;
    }
}
