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
            shadeIntensity: 0.5,

            colorScale: {
                ranges: [{
                    from: 0,
                    to: 50,
                    color: "#0000ff",
                    name: "undefined",
                }]
            }
        }
    },
    series: [],
    dataLabels: {
        enabled: false
    },
    title: {
        text: 'HeatMap Chart with Color Range'
    }
};

var HeatMap = new ApexCharts(document.querySelector("#HeatMap"), options);
HeatMap.render();
var ser = [
    {
        name: "Series 1",
        data: [45, 52, 38, 24, 33, 26, 21, 20, 6, 8, 15, 10]
    }
];
HeatMap.appendSeries(ser);
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
            }
        });
    console.log("p: ");
    console.log(p);
}

function checkCategories() {
    $.getJSON("../api/strategicIndicators/categories")
        .then (function(categories) {
            if (categories.length === 0) {
                alert("You need to define Strategic Indicator categories in order to see the heatmap correctly. " +
                    "Please, go to the Categories section of the Configuration menu and define them.");
            } else {
                var f = 0;
                var plus = (100 / categories.length);
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
            }
        });
}



function getData() {
    var today = new Date();
    var aux = [];
    var todayTextDate = parseDate(today);
    // must be the datefrom of first phase
    var date = new Date(today.getFullYear(), 0, 1);
    var textDate = parseDate(date);
    $.ajax({
        url: "../api/strategicIndicators/historical?" + "from=" + textDate + "&to=" + todayTextDate,
        type: "GET",
        success: function(data) {
            if (data.length === 0) {
                alert("No data about Strategic Indicators for phases of this project.");
            } else {
                console.log(data);
                data.forEach(function (d) {
                    if (aux.length == 0) {
                        s.push({
                            name: d.name,
                            data: [{x: "P1", y: 1}, {x: "P2", y: 5}, {x: "P3", y: 8}]
                        });
                        aux.push(d.name);
                    } else {
                        if (!aux.includes(d.name)) {
                            s.push({
                                name: d.name,
                                data: [{x: "P1", y: 1}, {x: "P2", y: 5}, {x: "P3", y: 8}]
                            });
                            aux.push(d.name);
                        }
                    }
                });
            }
        }
    });
    console.log("series: ");
    console.log(s);
}

function mode(arr) {
    var numMapping = {};
    var greatestFreq = 0;
    var mode;
    arr.forEach(function findMode(number) {
        numMapping[number] = (numMapping[number] || 0) + 1;

        if (greatestFreq < numMapping[number]) {
            greatestFreq = numMapping[number];
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

function generateData(count, yrange) {
    var i = 0;
    var series = [];
    while (i < count) {
        var x = (i + 1).toString();
        var y = Math.floor(Math.random() * (yrange.max - yrange.min + 1)) + yrange.min;

        series.push({
            x: x,
            y: y
        });
        i++;
    }
    return series;
}




