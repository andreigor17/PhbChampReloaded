/* 
 *  
 */

function adicionarCoordenada(polygon, coordenada, map) {
    if (polygon.getLatLngs().length == 0) {
        map.setView([coordenada.latitude, coordenada.longitude], 18);
    }

    polygon.addLatLng([coordenada.latitude, coordenada.longitude]);
}

function gerarPoligono(polygon, data, map) {
    var coordenadas = [];

    for (var i = 0; i < data.length; i++) {
        coordenadas.push([data[i].latitude, data[i].longitude]);
    }

    map.setView([data[0].latitude, data[0].longitude], 18);

    polygon.setLatLngs(coordenadas);
}

function plotarConflito(areasConflitados, imoveisConflitados, map) {

    var polygon;
    var coordenadas;
    var area;
    var i;
    var j;

    for (i = 0; i < areasConflitados.length; i++) {

        polygon = L.polygon([], {
            color: '#ff0b00',
            fillColor: '#ff0b00',
            weight: 1
        });

        coordenadas = [];
        
        area = areasConflitados[i];

        for (j = 0; j < area.length; j++) {
            coordenadas.push([area[j].latitude, area[j].longitude]);
        }

        polygon.addTo(map);

        polygon.setLatLngs(coordenadas);
    }

    for (i = 0; i < imoveisConflitados.length; i++) {

        polygon = L.polygon([], {
            color: '#f1ff00',
            fillColor: '#f1ff00',
            weight: 1
        });

        coordenadas = [];
        
        area = imoveisConflitados[i];

        for (j = 0; j < area.length; j++) {
            coordenadas.push([area[j].latitude, area[j].longitude]);
        }

        polygon.addTo(map);

        polygon.setLatLngs(coordenadas);
    }

}
