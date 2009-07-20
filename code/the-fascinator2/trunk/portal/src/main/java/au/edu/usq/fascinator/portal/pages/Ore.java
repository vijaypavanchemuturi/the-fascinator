/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.portal.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;

public class Ore extends Detail {

    @Inject
    private RequestGlobals requestGlobals;

    @Override
    Object onActivate(Object[] params) {
        super.onActivate(params);
        // HttpServletRequest request = requestGlobals.getHTTPServletRequest();
        // try {
        // Aggregation agg = OREFactory.createAggregation(new URI(
        // request.getRequestURL().toString()));
        // ResourceMap rem = agg.createResourceMap(new URI(
        // request.getRequestURL().append("#aggregation").toString()));
        //
        // for (DatastreamType dsType : getDatastreams()) {
        // AggregatedResource ar = agg.createAggregatedResource(new URI(
        // request.getRequestURL()
        // .append("/")
        // .append(dsType.getDsid())
        // .toString()));
        // agg.addAggregatedResource(ar);
        // }
        //
        // ORESerialiser serializer =
        // ORESerialiserFactory.getInstance("RDF/XML");
        // ResourceMapDocument doc = serializer.serialise(rem);
        // String serialized = doc.toString();
        // return new BinaryStreamResponse("application/rdf+xml",
        // new ByteArrayInputStream(serialized.getBytes("UTF-8")));
        // } catch (URISyntaxException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // } catch (OREException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (ORESerialiserException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return null;
    }
}
