package com.upc.gessi.qrapids.app.domain.adapters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qr.QRGenerator;

@Component
public class QRGeneratorFactory {

    @Value("${pabre.url}")
    String pabreUrl;

    public QRGenerator getQRGenerator () {
        return new QRGenerator(pabreUrl);
    }
}
