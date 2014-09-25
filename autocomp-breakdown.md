Autonomous Components Title records Task Breakdown
===============================================

This is just temporary file, and should only live as long as the tasks

### Opgave: Opgradér Summa til Solr
Der kan være ting her med plug-n-pray for vores eksisterende queries. Det kan komme til at gribe om sig. Vi testet doms summa storage med solr nogensinde, men web mener de er API kompatible.

### Opgave: Opdater Summa-indeks til items i stedet for batches.
Inkluder timestamps, nyeste timestamp for hver eventtype og content model indekset. Brug vores fix til lastModified så ændringer til EVENTS datastreamen ikke er med.

### Opgave: Opdater til at triggering kan ske med mange flere settings
Flere settings end de tre lister af Events som vi hidtil brugte.
Dvs. alle de settings der er nævnt i det nye design

### Opgave: Omdøb Batch til Item 
Søg efter ordet batch i batch-event-framework.
Overvej at skifte ud med item Ved samme lejlighed fang lige ting der hedder noget med newspaper, de er der vist stadig i maven groupIds eller java pakkenavne

### Opgave: Queued work
Giv mulighed for en kø af flere Items der accepteres af en komponent end der kan arbejdes på samtidig

### Opgave: Summa search without limits
Fjern limit på 1000 i summa-søgninger og erstat det med paging. Dette kræver at resultaterne er fornuftigt sorteret

### Opgave: EVENTS are editable on published objects
EVENTS datastreams kan skrives til på låste objekter. Dette er en opdatering af XACML sikkerhedspolitikken i fedora

### Opgave: Content Model Item
Opdater baseObjectIngest til at RoundTrip-objekter er Items. Dette kræver at man har lavet ContentModel_Item

### Opgave: Test eksisterende SBOI-funktionalitet

### Opgave: Opdater baseObjectIngest til at edition- og newspaper-objekter er Items

### Opgave: Nye autocoms
Lav to nye autonome komponenter der trigger'er på hhv. ContentModel_Edition og ContentModel_Newspaper

### Opgave: Add newspaper timestamps to SBOI
Indekser periode for Newspaper-objekter i SBOI og dato for Edition-objekter i SBOI (dette er egentlig misbrug, men det er svært ellers at sikre synkronicitet mellem trigger og at finde de rette editions)
ABR: Jeg er ikke helt glad for denne her. Det er en fair begrundelse, men jeg er bekymret for misbrug. Hvis bare vi holder det separat kan det dog nok gå.

### Opgave: Implementer runnableComponent på ContentModel_Newspaper:
 * Søg efter alle edition-objekter der har en dato indenfor perioden
 * Find listen af editions der allerede peger på Newspaper-objektet
 * Tilføj og fjern relationer efter behov

### Opgave: Implementer runnableComponent på ContentModel_Edition:
 * Søg efter alle newspaper-objekter der har en periode med denne dato i
 * Find listen af newspaper-objekter der peges på
 * Tilføj og fjern relationer efter behov
