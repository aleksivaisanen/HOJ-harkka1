# Hajautetut ohjelmistoj‰rjestelm‰t ja pilvipalvelut -kurssin harjoitustyˆ nro. 1

## Teht‰v‰nanto:
### Teht‰v‰n‰ on tehd‰ sovellus X, joka tarjoaa lukujen summauspalvelua erikseen m‰‰ritellyn palvelimen Y
k‰yttˆˆn. Palvelin Y on valmiiksi toteutettu ja sen yhteyteen on m‰‰ritelty protokolla, jonka avulla
summauspalvelua tarjoava sovellus X voi ottaa siihen yhteytt‰. 

1. Sovellus X ottaa yhteytt‰ UDP-protokollalla palvelimen Y porttiin 3126 v‰litt‰en yhden
UDP-paketin, joka sis‰lt‰‰ TCP-portin numeron, johon palvelimen Y halutaan ottavan
yhteytt‰ ja jota sovellus X kuuntelee.

2. Palvelin Y ottaa yhteyden ilmaistuun porttiin z koneessa, josta UDP-pyyntˆ tuli 1...5
sekunnin kuluessa pyynnˆn l‰hett‰misest‰. Jos sovellus X ei saa t‰llaista TCP-pohjaista
yhteydenottoa ko. ajan puitteissa, tulee sen l‰hett‰‰ edellisen kohdan UDP-viesti uudestaan.
Viiden ep‰onnistuneen yrityskerran j‰lkeen sovelluksen X tulee lopettaa itsens‰.

3. Kun TCP-yhteys on muodostunut X:n ja Y:n v‰lille, keskustelu niiden v‰lill‰ tapahtuu
jatkossa oliovirtaa (ObjectInputStream / ObjectOutputStream) k‰ytt‰en. Kyseisess‰ virrassa
v‰litet‰‰n vain kokonaislukuja. Ensin palvelin Y ilmoittaa X:lle kokonaisluvun t v‰lilt‰
2...10. Jos t‰llaista lukua ei tule 5 sekunnin kuluessa, tulee X:n lopettaa itsens‰ hallitusti
v‰litetty‰‰n ensin luvun -1 palvelimelle Y.

4. Sovellus X vastaa l‰hett‰m‰ll‰ takaisin t kappaletta kokonaislukuja P1, P2,..., Pt, jotka ovat
sellaisten porttien numeroita, joissa sovelluksen X hallinnoimat summauspalvelijat toimivat.

5. Saatuaan tiet‰‰ porttinumerot P1, P2,Ö, Pt, palvelin Y tyˆllist‰‰ kyseisi‰
summauspalvelijoita v‰litt‰m‰ll‰ niille oliovirran yli kokonaislukuja. V‰litett‰vien lukujen
m‰‰r‰‰ ei tiedet‰ etuk‰teen. Palvelin Y v‰litt‰‰ kullekin summauspalvelijalle lukusarjan,
jonka viimeinen luku on nolla. Kun summauspalvelija vastaanottaa luvun nolla, sen tulee
lopettaa itsens‰ ja sulkea yhteys palvelimeen Y. Summauspalvelijat eiv‰t v‰lit‰ mit‰‰n tietoa
suoraan palvelimelle Y, vaan ne kartuttavat v‰litettyjen lukujen summaa ja lukum‰‰r‰‰
sovelluksen X ìosoittamaanî paikkaan. X osoittaa kullekin summauspalvelijalle ìlokerotî,
jonne ne voivat ker‰t‰ ko. tietoja.

6. Samalla kun palvelin Y tyˆllist‰‰ summauspalvelijoita, se voi kysy‰ sovellukselta
kolmenlaista tietoa: (1) mik‰ on t‰h‰n menness‰ v‰litettyjen lukujen kokonaissumma, (2)
mille summauspalvelijalle v‰litettyjen lukujen summa on suurin ja (3) mik‰ on t‰h‰n
menness‰ kaikille summauspalvelimille v‰litettyjen lukujen kokonaism‰‰r‰. Edelliset utelut
palvelin Y tekee v‰litt‰m‰ll‰ X:lle niiden v‰lisen oliovirran yli kokonaisluvun 1, 2 tai 3
(vastaavasti). Sovelluksen X tulee vastata takaisin sen hetkisen tilanteen mukaisella
kokonaisluvulla. Jos sovellus X saa t‰ss‰ utelutilassa palvelimelta Y jonkin muun numeron
kuin 1, 2, 3 tai 0 (selitys seuraavassa kohdassa), niin sen tulee vastata takaisin luvulla -1.


7. Palvelin Y voi suorittaa edellisi‰ kyselyit‰ useita ja osa summauspalvelijoista voi
kysymyssarjan aikana p‰‰tty‰ (koska Y on v‰litt‰nyt niille nollan). Kun palvelin Y p‰‰tt‰‰
lopettaa X:n tarjoaman palvelun k‰ytˆn, se v‰litt‰‰ X:lle luvun nolla. Sen seurauksena
sovelluksen X tulee sulkea TCP-yhteys palvelimeen Y, lopettaa mahdollisesti kesken olevat
summauspalvelijat ja lopulta itse sovelluksen X suorituksen. Jos uteluissa on yli minuutin
tauko, tulee X:n sulkea itsens‰ hallitusti.