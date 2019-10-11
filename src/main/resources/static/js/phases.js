var r = []; // ranges
var p = []; // phases
var s = []; // series

var options = {
    chart: {
        height: 350,
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
    series: []
};

var HeatMap = new ApexCharts(document.querySelector("#HeatMap"), options);
HeatMap.render();


function getPhasesList () {
    $.getJSON("../api/phases")
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
            }
        });
}

function checkCategories() {
    $.getJSON("../api/strategicIndicators/categories")
        .then (function(categories) {
            if (categories.length === 0) {
                alert("You need to define Strategic Indicator categories in order to see the heatmap correctly. " +
                    "Please, go to the Categories section of the Configuration menu and define them.");
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
                var plus = (100 / categories.length);
                categories.reverse();
                categories.forEach(function (cat) {
                    var aux = Math.round(f);
                    var aux2 = Math.round(f+plus);
                    r.push({
                        from: aux,
                        to: aux2,
                        name: cat.name,
                        color: cat.color
                    });
                    f += (plus);
                });
                console.log("r: ");
                console.log(r);
                HeatMap.updateOptions({  plotOptions: {
                        heatmap: {
                            enableShades: false,
                            colorScale: {
                                ranges: r,
                            },
                        }
                    }
                });
            }
        });
}



function getData(phases) {
    var today = new Date();
    var todayTextDate = parseDate(today);
    $.getJSON("../api/strategicIndicators/historical?" + "from=" + phases[0].from + "&to=" + todayTextDate)
        .then (function(data) {
            if (data.length === 0) {
                alert("No data about Strategic Indicators for phases of this project.");
            } else {
                var aux = [-1];
                var values = [];
                var currentSI = data[0].name;
                var i = 0;
                var currentPH = phases[i];
                data.forEach(function (d) {
                    if (d.name == currentSI) {
                        var out = false;
                        while (i < phases.length && !out) {
                            if (d.date < currentPH.to) {
                                aux.push(d.value.first);
                                out = true;
                            } else {
                                var m = 100 * mode(aux);
                                values.push({x: currentPH.name, y: m});
                                i++;
                                currentPH = phases[i];
                                aux = [-1];
                            }
                        }
                    } else {
                        var m = 100 * mode (aux);
                        values.push( {x: currentPH.name, y: m});
                        i++;
                        currentPH = phases[i];
                        aux = [-1];
                        while (i < phases.length){
                            var m = 100 * mode (aux);
                            values.push( {x: currentPH.name, y: m});
                            i++;
                            currentPH = phases[i];
                            aux = [-1];
                        }
                        s.push({
                            name: currentSI,
                            data: values
                        });
                        currentSI = d.name;
                        aux = [-1];
                        i = 0;
                        values = [];
                        currentPH = phases[i];
                        out = false;
                        while (i < phases.length && !out) {
                            if (d.date < currentPH.to) {
                                aux.push(d.value.first);
                                out = true;
                            } else {
                                var m = 100 * mode(aux);
                                values.push({x: currentPH.name, y: m});
                                i++;
                                currentPH = phases[i];
                                aux = [-1];
                            }
                        }
                    }
                });
                var m = 100 * mode (aux);
                values.push( {x: currentPH.name, y: m});
                i++;
                currentPH = phases[i];
                aux = [-1];
                while (i < phases.length){
                    var m = 100 * mode (aux);
                    values.push( {x: currentPH.name, y: m});
                    i++;
                    currentPH = phases[i];
                    aux = [-1];
                }
                s.push({
                    name: currentSI,
                    data: values
                });
                console.log("new serie: ");
                console.log(s);
                HeatMap.updateSeries(s);
            }
        }
    );
}

function mode(arr) {
    var numMapping = {};
    var greatestFreq = 0;
    var mode;
    if (arr.length == 1){
        return arr[0];
    } else {
        arr.shift();
        arr.forEach(function findMode(number) {
            numMapping[number] = (numMapping[number] || 0) + 1;

            if (greatestFreq < numMapping[number]) {
                greatestFreq = numMapping[number];
                mode = number;
            }
        });
        return mode;
    }
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





