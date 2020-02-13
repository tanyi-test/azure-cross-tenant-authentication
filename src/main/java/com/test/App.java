package com.test;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.GenericResourceInner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {

        final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

         ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);
         ApplicationTokenCredentials credentials2 = ApplicationTokenCredentials.fromFile(new File(System.getenv("AZURE_AUTH_LOCATION_2")));

         Azure azure = Azure.configure().withAuxiliaryCredentials(credentials2).authenticate(credentials).withDefaultSubscription();
//         Azure azure = Azure.authenticate(credentials).withDefaultSubscription();

        azure.resourceGroups().define("vnet1").withRegion(Region.US_WEST).create();

//        String name = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().substring(0, 16);
        String name1 = "lkjsdflkjsdfvnet1";
        String name2 = "lkjsdflkjsdfvnet2";

        azure.networks().define(name1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup("vnet1")
                .withAddressSpace("10.0.0.0/24")
                .create();
        Network network1 = azure.networks().getByResourceGroup("vnet1", name1);

        Azure azure2 = Azure.authenticate(credentials2).withDefaultSubscription();

        azure2.networks().define(name2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup("vnet2")
                .withAddressSpace("10.0.1.0/24")
                .create();
        Network network2 = azure2.networks().getByResourceGroup("vnet2", name2);

        String peering1 = "peer1-peer2";

//        VirtualNetworkPeeringInner inner = new VirtualNetworkPeeringInner()
//                .withAllowVirtualNetworkAccess(true)
//                .withAllowForwardedTraffic(false)
//                .withAllowGatewayTransit(false)
//                .withUseRemoteGateways(false)
//                .withRemoteVirtualNetwork(new SubResource().withId(network2.id()));
//        azure.networks().manager().inner().virtualNetworkPeerings().beginCreateOrUpdate("vnet1", name1, peering1, inner);
//        azure.networks().manager().inner().virtualNetworkPeerings().beginDelete("vnet1", name1, peering1);

        Map<String, Object> properties = new HashMap<>();
        properties.put("allowVirtualNetworkAccess", true);
        properties.put("allowForwardedTraffic", false);
        properties.put("allowGatewayTransit", false);
        properties.put("useRemoteGateways", false);
        properties.put("remoteVirtualNetwork", new HashMap<String, String>() {
            {put("id", network2.id());}
        });
        GenericResourceInner genericResourceInner = new GenericResourceInner()
                .withProperties(properties);
        azure.genericResources().manager().inner().resources().beginCreateOrUpdate("vnet1",
                "Microsoft.Network",
                String.format("virtualNetworks/%s/", name1),
                "virtualNetworkPeerings",
                "peer1-peer2",
                "2019-08-01",
                genericResourceInner);
    }
}
