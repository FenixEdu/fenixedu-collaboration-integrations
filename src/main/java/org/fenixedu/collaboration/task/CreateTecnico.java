package org.fenixedu.collaboration.task;

import com.google.gson.JsonObject;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.collaboration.domain.azure.Client;

public class CreateTecnico extends CustomTask {

    private JsonObject getTecnico() {
        final JsonObject body = new JsonObject();
        body.addProperty("displayName", "Instituto Superior Técnico");
        body.addProperty("description", "Técnico Lisboa - School of Engineering");
        body.addProperty("status", "active");
//        body.addProperty("externalSource", "");
        body.addProperty("principalEmail", "cg@tecnico.ulisboa.pt");
        body.addProperty("principalName", "Rogério Colaço");
//        body.addProperty("externalPrincipalId", "");
        body.addProperty("highestGrade", "PHD");
        body.addProperty("lowestGrade", "Licenciatura");
        body.addProperty("schoolNumber", "1");
        final JsonObject address = new JsonObject();
        address.addProperty("city", "Lisboa");
        address.addProperty("countryOrRegion", "Portugal");
        address.addProperty("postalCode", "1049-001");
//        address.addProperty("state", "");
        address.addProperty("street", "Av. Rovisco Pais, 1");
        body.add("address", address);
//        body.addProperty("externalId", "");
        body.addProperty("phone", "");
        return body;
    }

    @Override
    public void runTask() throws Exception {
        final JsonObject result = Client.createSchool(getTecnico());
        taskLog("%s%n", result.toString());
        taskLog("Done.");
    }

}
