import sum.ereignis.*;
import sum.komponenten.*;
import sum.werkzeuge.*;
import sum.strukturen.Liste;  
import sum.komponenten.Auswahl;

/**
 * Kartenmanager_EWH:
 * - Verwendet eine Liste<Karte> anstelle von Arrays.
 * - Einfügen, Entfernen, Darstellen wie vorher.
 * - NEU: Zwei beispielhafte Sortierverfahren:
 *        1) insertionSortListe()
 *        2) mergeSortListe()
 */
public class Kartenmanager_SuS extends Ereignisanwendung {

    // ----------------------------------------------------------
    // 1) GUI-Objekte
    // ----------------------------------------------------------
    private Knopf startKnopf;   
    private Knopf updateKnopf;  
    private Knopf einfKnopf;    
    private Knopf entfKnopf;    

    private Etikett infoEtikett;    
    private Etikett labelStapelsize; 
    private Etikett labelWert;  
    private Etikett labelFarbe; 
    private Etikett labelPos;   

    private Auswahl auswahlWert;   
    private Auswahl auswahlFarbe;  

    private Textfeld tfUmfang;  
    private Textfeld tfPos;     

    private Buntstift stift;

    // ----------------------------------------------------------
    // 2) Listen-Datenstruktur
    // ----------------------------------------------------------
    // Innere Klasse Karte
    private class Karte {
        int wert;   // 1..13 => "2..Ass"
        int farbe;  // 0..3  => "Kreuz..Pik"

        public Karte(int w, int f) {
            this.wert  = w;
            this.farbe = f;
        }
    }

    private Liste<Karte> karten;   
    private int kartenAnzahl = 53;  

    private String[] werteNamen = {
       "", "2", "3", "4", "5", "6", 
       "7", "8", "9", "10", "Bube", "Dame", "König", "Ass"
    };
    private String[] farbenNamen = {
       "Kreuz", "Karo", "Herz", "Pik"
    };

    private long startZeit;
    private long endZeit;

    public Kartenmanager_SuS() {
        super();

        stift = new Buntstift();

        // 3) GUI
        infoEtikett    = new Etikett( 50,  20, 400, 30, 
            "Karten mit Liste + Sortierverfahren (Insertion).");

        labelStapelsize= new Etikett( 50,  60, 80, 25, "Stapelgr.:");
        tfUmfang       = new Textfeld(130, 60, 60, 25, "52"); 
        updateKnopf    = new Knopf(200, 60, 100, 30, "Erneuern");
        startKnopf     = new Knopf(310, 60, 100, 30, "Sortieren");

        labelWert   = new Etikett(450,  20, 40, 25, "Wert:");
        auswahlWert = new Auswahl(500, 20, 80, 25);

        labelFarbe  = new Etikett(600, 20, 50, 25, "Farbe:");
        auswahlFarbe= new Auswahl(660,20, 80, 25);

        labelPos    = new Etikett(450, 60, 60, 25, "Position:");
        tfPos       = new Textfeld(520, 60, 60, 25, "0");

        einfKnopf   = new Knopf(600, 60, 100, 30, "Einfügen");
        entfKnopf   = new Knopf(710, 60, 100, 30, "Entfernen");

        // Auswahllisten füllen: index=0..12 => "2..Ass"
        for(int w=1; w<=13; w++){
            auswahlWert.haengeAn(werteNamen[w]);
        }
        for(int f=0; f<4; f++){
            auswahlFarbe.haengeAn(farbenNamen[f]);
        }

        // Liste anlegen, init
        karten = new Liste<Karte>();
        initialisiereKarten(0);

        // Ereignismethoden
        startKnopf.setzeBearbeiterGeklickt("Sort_Klick");
        updateKnopf.setzeBearbeiterGeklickt("Update_Klick");
        einfKnopf.setzeBearbeiterGeklickt("Einfuegen_Klick");
        entfKnopf.setzeBearbeiterGeklickt("Entfernen_Klick");

        // Zeichnen ab y=150
        zeichneKarten(0, 50, 150);

        this.fuehreAus();
    }

    // ----------------------------------------------------------
    // (A) Initialisieren & Zeichnen
    // ----------------------------------------------------------
    void initialisiereKarten(int i) {
        if(i>=kartenAnzahl) return;
        int w = (int)(Math.random()*13)+1; 
        int f = (int)(Math.random()*4);

        karten.zumEnde();
        karten.fuegeDahinterEin(new Karte(w,f)); 
        initialisiereKarten(i+1);
    }

    void zeichneKarten(int index, int x, int y) {
        if(index>=karten.laenge()) return;

        karten.geheZuPosition(index);
        Karte c = karten.aktuellesElement();
        if(c==null) {
            // Falls doch mal null => überspringen
            zeichneKarten(index+1, x,y);
            return;
        }

        String name = farbenNamen[c.farbe] + " " + werteNamen[c.wert];
        zeichneEineKarte(x,y,name);

        x+=90;
        if(index%13==0){
            x=50;
            y+=130;
        }
        zeichneKarten(index+1,x,y);
    }

    void zeichneEineKarte(int x,int y,String name){
        stift.bewegeBis(x,y);
        stift.zeichneRechteck(80,120);
        stift.bewegeBis(x+5,y+50);
        stift.schreibeText(name);
    }

    void loescheAnzeige() {
        stift.radiere();
        zeichneKarten(0, 50, 150);
        stift.normal();
    }

    // ----------------------------------------------------------
    // (B) Einfügen & Entfernen
    // ----------------------------------------------------------
    void fuegeKarteEin(int pos,int wert,int farbe) {
        if(pos<0 || pos>karten.laenge()){
            infoEtikett.setzeInhalt("Pos="+pos+" ungültig (0.."+karten.laenge()+")");
            return;
        }
        karten.geheZuPosition(pos);
        karten.fuegeDahinterEin(new Karte(wert,farbe));
        kartenAnzahl = karten.laenge();
    }

    void entferneKarte(int pos){
        if(pos<0 || pos>=karten.laenge()){
            infoEtikett.setzeInhalt("Pos="+pos+" ungültig (0.."+(karten.laenge()-1)+")");
            return;
        }
        if(karten.laenge()<=1){
            infoEtikett.setzeInhalt("Stapel zu klein, nichts entfernt!");
            return;
        }
        karten.geheZuPosition(pos);
        karten.loescheAktuelles();
        kartenAnzahl= karten.laenge();
    }

    // ----------------------------------------------------------
    // (C) Sortierverfahren
    // ----------------------------------------------------------

    // Vergleichsmethode:
    // Sortieren nach (wert, farbe)
    private int compare(Karte a, Karte b) {
        if(a.wert!= b.wert) return a.wert - b.wert;
        return a.farbe - b.farbe;
    }

    /**
     * insertionSortListe():
     *  - Wir erzeugen eine neue, leere Liste "sorted"
     *  - Wir entnehmen aus 'karten' jeweils die erste Karte
     *    und fügen sie an passender Stelle in "sorted" ein.
     *  - Ergebnis: sorted ist aufsteigend sortiert
     *  - Kopieren es zurück in karten oder verweisen direkt.
     */
    void insertionSortListe() {
        
        for(int i = 1; i < karten.laenge(); i++) {
            // Block 1: Aktuelles Element holen
            karten.geheZuPosition(i);
            Karte a = karten.aktuellesElement();
            //if(a == null) continue; // Null-Check
    
            // Block 2: Einfügeposition suchen
            int j = i-1;
            boolean einsortiert = false;
        
            while(j >= 0 && !einsortiert) {
                karten.geheZuPosition(j);
                Karte b = karten.aktuellesElement();
                
                /*if(vgl == null) { // Null-Check für Vergleichselement
                    j--;
                    continue;
                }*/
            
                // Vergleich direkt im Code (ohne compare()-Methode)
                if(a.wert < b.wert || (a.wert == b.wert && a.farbe < b.farbe)) {
                    j--;
                } else {
                    einsortiert = true;
                }
            }
        
            // Block 3: Element verschieben
            karten.geheZuPosition(i);
            karten.loescheAktuelles();
            karten.geheZuPosition(j+1);
            karten.fuegeDavorEin(a);
        }
    }
    
    // ----------------------------------------------------------
    // (D) Ereignismethoden
    // ----------------------------------------------------------
    public void Sort_Klick() {
        loescheAnzeige();
        startZeit = System.currentTimeMillis();

        // Beispiel: 500 Durchläufe
        for(int i=0; i<1000; i++){
            // neu init
            karten = new Liste<Karte>();
            //kartenAnzahl=52;
            initialisiereKarten(0);

            // Eines der Sortierverfahren (z.B. Insertion):
            // insertionSortListe();
            // oder
            insertionSortListe();
        }

        endZeit=System.currentTimeMillis();
        double d= endZeit - startZeit;

        // Jetzt ist 'karten' sortiert
        zeichneKarten(0, 50, 150);
        infoEtikett.setzeInhalt(String.format("Sort: %.2f ms (10000 Durchläufe)", d));
    }

    public void Update_Klick() {
        String s= tfUmfang.inhaltAlsText().trim();
        int neuAnz;
        try {
            neuAnz= Integer.parseInt(s)+1;
            if(neuAnz<=0) neuAnz=53;
        }catch(NumberFormatException ex){
            neuAnz=53;
        }
        loescheAnzeige();
        karten= new Liste<Karte>();
        kartenAnzahl= neuAnz;
        initialisiereKarten(0);
        zeichneKarten(0,50,150);

        infoEtikett.setzeInhalt("Neuer Stapel: "+(kartenAnzahl-1)+" Karten (Liste).");
    }

    public void Einfuegen_Klick() {
        String sPos= tfPos.inhaltAlsText().trim();
        int pos;
        try{
            pos= Integer.parseInt(sPos);
        } catch(NumberFormatException ex){
            infoEtikett.setzeInhalt("Fehler: ungültige Position!");
            return;
        }
        int wIndex= auswahlWert.index();
        int fIndex= auswahlFarbe.index();
        int wert= wIndex+1; 
        int farbe= fIndex;

        loescheAnzeige();
        fuegeKarteEin(pos, wert, farbe);
        zeichneKarten(0,50,150);

        String wName = farbenNamen[farbe]+" "+werteNamen[wert];
        infoEtikett.setzeInhalt("Eingefügt: "+wName+" an Pos="+pos+
            " (Länge="+kartenAnzahl+")");
    }

    public void Entfernen_Klick() {
        String sPos= tfPos.inhaltAlsText().trim();
        int pos;
        try {
            pos= Integer.parseInt(sPos);
        }catch(NumberFormatException ex){
            infoEtikett.setzeInhalt("Fehler: ungültige Position!");
            return;
        }

        loescheAnzeige();
        entferneKarte(pos);
        zeichneKarten(0,50,150);

        infoEtikett.setzeInhalt("Karte entfernt an Pos="+pos+
            " (Länge="+kartenAnzahl+")");
    }

    // ----------------------------------------------------------
    // Hilfsmethode zum Kartenvergleich
    // ----------------------------------------------------------
}
