var r = []; // ranges
var p = []; // phases
var s = []; // series

var options = {
    chart: {
        height: 400,
        type: 'heatmap',
    },
    plotOptions: {
        heatmap: {
            enableShades: false,
            colorScale: {
                ranges: []
            }
        }
    },
    dataLabels: {
        enabled: false
    },
    series: [],

    noData: {
        text: "Processing data...",
        align: 'center',
        verticalAlign: 'middle',
        offsetX: 0,
        offsetY: 0,
        style: {
            color: undefined,
            fontSize: '24px',
            fontFamily: 'Helvetica, Arial, sans-serif'
        }
    },
    tooltip: {
        enabled: false
    }
};

var HeatMap = new ApexCharts(document.querySelector("#HeatMap"), options);
HeatMap.render();


function getPhasesList () {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/phases";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url)
        .then (function(phases) {
            if (phases.length > 0) {
                phases.forEach(function (ph) {
                    p.push({
                        from: ph.dateFrom,
                        to: ph.dateTo,
                        name: ph.name
                    });
                });
                getData(p);
            } else {
                warningUtils("Error", "No information about phases of this project.");
                //alert("No information about phases of this project.");
            }
        });
}

function checkCategories() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/strategicIndicators/categories";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url)
        .then (function(categories) {
            if (categories.length === 0) {
                warningUtils("Warning", "You need to define Strategic Indicator categories in order to see the heatmap correctly. " +
                    "Please, go to the Categories section of the Configuration menu and define them.");
                //alert("You need to define Strategic Indicator categories in order to see the heatmap correctly. " +
                  //  "Please, go to the Categories section of the Configuration menu and define them.");
            } else {
                // add special color for SI with no data
                r.push({
                    from: -100,
                    to: -100,
                    name: "No data",
                    color: "#cccccc"
                });
                // add other colors for each category
                var f = 0;
                var plus = Math.round(100 / categories.length);
                categories.reverse();
                categories.forEach(function (cat) {
                    var aux = Math.round(f);
                    var aux2 = Math.round(f+plus);
                    r.push({
                        from: aux,
                        to: Math.min(aux2, 100),
                        name: cat.name,
                        color: cat.color
                    });
                    f += (plus+1);
                });
                console.log("r: ");
                console.log(r);
                HeatMap.updateOptions({  plotOptions: {
                        heatmap: {
                            enableShades: false,
                            colorScale: {
                                ranges: r
                            }
                        }
                    }
                });
            }
        });
}



function getData(phases) {
    var today = new Date();
    var todayTextDate = parseDate(today);

    var serverUrl = sessionStorage.getItem("serverUrl");
    var profileId = sessionStorage.getItem("profile_id");

    var url = "/api/strategicIndicators/historical?profile="+profileId;
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url + "&from=" + phases[0].from + "&to=" + todayTextDate)
        .then (function(data) {

            console.log("getData");
            console.log(data);
            console.log("phases");
            console.log(phases);

            if (data.length === 0) { // when there is NO historical data for phases period
                var siData = [];
                addNoDataStrategicIndicators (phases, siData);
            } else { // when there is historical data
                var aux = [{cat: "No data", val:-1}];
                var values = [];
                var currentSI = data[0].name; // take first SI from hist. data
                var i = 0;
                var currentPH = phases[i]; // take first phase as current phase
                var siData = [data[0].name];
                data.forEach(function (d) { // for on historical data
                    if (d.name == currentSI) { // same SI
                        var out = false;
                        while (i < phases.length && !out) {
                            if (d.date <= currentPH.to) {
                                aux.push({cat: d.value.second, val: d.value.first});
                                out = true;
                            } else {
                                var m = mode(aux);
                                values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                                // change phase in same SI
                                i++;
                                currentPH = phases[i];
                                aux = [{cat: "No data", val:-1}];
                            }
                        }
                    } else {
                        // put current si values
                        var m = mode(aux);
                        values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                        i++;
                        currentPH = phases[i];
                        aux = [{cat: "No data", val:-1}];
                        // put no data for future phases of current si
                        while (i < phases.length){
                            var m = mode(aux);
                            values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                            i++;
                            currentPH = phases[i];
                            aux = [{cat: "No data", val:-1}];
                        }
                        // prepare heatmap series
                        s.push({
                            name: currentSI,
                            data: values
                        });
                        // change SI
                        currentSI = d.name;
                        siData.push(d.name);
                        aux = [{cat: "No data", val:-1}];
                        i = 0;
                        values = [];
                        currentPH = phases[i];
                        out = false;
                        while (i < phases.length && !out) {
                            if (d.date <= currentPH.to) {
                                aux.push({cat: d.value.second, val: d.value.first});
                                out = true;
                            } else {
                                var m = mode(aux);
                                values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                                i++;
                                currentPH = phases[i];
                                aux = [{cat: "No data", val:-1}];
                            }
                        }
                    }
                });
                // after for function on historical data
                // put last phase with historical data values (last si)
                var m = mode(aux);
                values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                // change phase
                i++;
                currentPH = phases[i];
                aux = [{cat: "No data", val:-1}];
                // add no data values for future phases if there are (last si)
                while (i < phases.length){
                    var m = mode(aux);
                    values.push({x: currentPH.name, y: Math.round(100 * m.val)});
                    i++;
                    currentPH = phases[i];
                    aux = [{cat: "No data", val:-1}];
                }
                // prepare heatmap series
                s.push({
                    name: currentSI,
                    data: values
                });
                addNoDataStrategicIndicators(phases, siData);
                drawHeatmap(phases);
            }
        }
    );
}

function addNoDataStrategicIndicators (phases, siData) {
    var profileId = sessionStorage.getItem("profile_id");
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/strategicIndicators?profile="+profileId;
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url)
        .then (function(data) {
            for (var i = 0; i < data.length; i++) {
                if (!siData.includes(data[i].name)){
                    var values = [];
                    for (var j = 0; j < phases.length; j++) {
                        values.push({x: phases[j].name, y: -100});
                    }
                    s.push({
                        name: data[i].name,
                        data: values
                    });
                }
            }
            console.log("new serie: ");
            console.log(s);
            drawHeatmap(phases);
        }
    );
}

function drawHeatmap(phases) {
    var h = 400;
    if (s.length >= 5) { h = 75 * s.length; }
    else{ h = 100 * s.length; }
    HeatMap.updateOptions({  chart: {
            height: h,
            type: 'heatmap',
        }
    });
    HeatMap.updateSeries(s);
    var x = 93;
    for (var i = 0; i < phases.length; i++) {
        x += (180*i);
        HeatMap.addXaxisAnnotation({
            x: x,
            strokeDashArray: 0,
            borderColor: 'transparent',
            fillColor: 'transparent',
            label: {
                borderColor: '#c2c2c2',
                borderWidth: 0,
                text: "(" + phases[i].from + " / " + phases[i].to + ")",
                textAnchor: 'middle',
                position: 'top',
                orientation: 'horizontal',
                offsetX: 0,
                offsetY: -15
            }
        });
        x = 93;
    }
}

function mode(arr) {
    var numMapping = {};
    var greatestFreq = 0;
    var mode;
    arr.forEach(function findMode(number) {
        numMapping[number.cat] = (numMapping[number.cat] || 0) + 1;

        if (greatestFreq <= numMapping[number.cat]) {
            greatestFreq = numMapping[number.cat];
            mode = number;
        }
    });
    return mode;
}

function parseDate(date) {
    var date = new Date(date);
    var dd = date.getDate();
    var mm = date.getMonth() + 1; //January is 0!
    var yyyy = date.getFullYear();

    if(dd < 10) {
        dd = '0' + dd;
    }
    if(mm < 10) {
        mm = '0' + mm;
    }

    var stringDate = yyyy + '-' + mm + '-' + dd;
    return stringDate
}





