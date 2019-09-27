// HeatMap Options
var data;
var myranges = [{
    from: 0,
    to: 34,
    name: 'Good',
    color: '#00A100'
},
    {
        from: 35,
        to: 67,
        name: 'Neutral',
        color: '#FFB200'
    },
    {
        from: 68,
        to: 100,
        name: 'Bad',
        color: '#FF0000'
    }
];

function checkCategories() {
    $.ajax({
        url: '../api/strategicIndicators/categories',
        type: "GET",
        success: function(categories) {
            if (categories.length === 0) {
                alert("You need to define Strategic Indicator categories in order to see the chart correctly. " +
                    "Please, go to the Categories section of the Configuration menu and define them.");
            } else {
                cat = categories;
                var f = 0;
                var plus = (100/categories.length);
                categories.forEach(function (cat) {
                    var aux = Math.round(f);
                    var aux2 = Math.round(f+plus);
                    myranges.push({
                        from: aux,
                        to: aux2,
                        name: cat.name,
                        color: cat.color
                    });
                    f += plus;
                });
                console.log(myranges);
            }
        }
    });
}
checkCategories();

function getData() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/strategicIndicators/current";
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (callData) {
            data = callData;
            //drawChart("gaugeChart", width, height, showButtons, chartHyperlinked, color);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                alert("Datasource connection failed.");
        }
    });
    console.log(data);
}

function sortDataAlphabetically () {
    function compare (a, b) {
        if (a.id < b.id) return -1;
        else if (a.id > b.id) return 1;
        else return 0;
    }
    data.sort(compare);
    console.log(data);
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


var options = {
    chart: {
        height: 350,
        type: 'heatmap',
    },
    plotOptions: {
        heatmap: {
            shadeIntensity: 0.5,

            colorScale: {
                ranges: myranges
                    /*[{
                    from: -30,
                    to: 5,
                    name: 'low',
                    color: '#00A100'
                },
                    {
                        from: 6,
                        to: 45,
                        name: 'high',
                        color: '#FFB200'
                    },
                    {
                        from: 46,
                        to: 55,
                        name: 'extreme',
                        color: '#FF0000'
                    }
                ]*/
            }
        }
    },
    dataLabels: {
        enabled: false
    },
    series: [{
        name: 'Jan',
        data: generateData(20, {
            min: 0,
            max: 100
        })
    },
        {
            name: 'Feb',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Mar',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Apr',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'May',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Jun',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Jul',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Aug',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        },
        {
            name: 'Sep',
            data: generateData(20, {
                min: 0,
                max: 100
            })
        }
    ],
    title: {
        text: 'HeatMap Chart with Color Range'
    },

}
// Init HeatMap
var HeatMap = new ApexCharts(document.querySelector("#HeatMap"), options);
// Render HeatMap
HeatMap.render();

