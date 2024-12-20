package com.example.zephyr.api.model;

import java.util.List;

public class WeatherResponse {
    public City city;
    public List<WeatherList> list;

    public class City {
        public String name;
        public String country;
    }

    public class WeatherList {
        public Main main;
        public List<Weather> weather;
        public Wind wind;
        public Rain rain;
        public String dt_txt;
        public Snow snow;

        public class Main {
            public float temp;
            public float temp_max; // Maximum temperature
            public float temp_min; //
            public float humidity;
            public float feels_like;
        }

        public class Weather {
            public String description;
            public String icon;
        }

        public class Wind {
            public float speed;
        }

        public class Rain {
            public float threeHourRain;
        }

        public class Snow {
            public float threeHourSnow;
        }
    }
}


