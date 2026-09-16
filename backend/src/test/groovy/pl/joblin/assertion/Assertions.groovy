package pl.joblin.assertion

import pl.joblin.application.IngestResult
import pl.joblin.domain.JobOffer
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot

class IngestResultAssert {
    private final IngestResult actual

    private IngestResultAssert(IngestResult actual) {
        this.actual = actual
    }

    static IngestResultAssert assertThat(IngestResult actual) {
        new IngestResultAssert(actual)
    }

    IngestResultAssert wasCreated() {
        assert actual.created
        this
    }

    IngestResultAssert wasUpdated() {
        assert !actual.created
        this
    }

    IngestResultAssert hasSameIdAs(IngestResult other) {
        assert actual.id == other.id
        this
    }
}

class OfferAssert {
    private final JobOffer actual

    private OfferAssert(JobOffer actual) {
        this.actual = actual
    }

    static OfferAssert assertThat(JobOffer actual) {
        new OfferAssert(actual)
    }

    OfferAssert hasTitle(String title) {
        assert actual.title == title
        this
    }

    OfferAssert hasCompany(String company) {
        assert actual.company == company
        this
    }

    OfferAssert hasSourceBot(SourceBot bot) {
        assert actual.sourceBot == bot
        this
    }

    OfferAssert hasStatus(OfferStatus status) {
        assert actual.status == status
        this
    }

    OfferAssert hasSourceUrl(String url) {
        assert actual.sourceUrl == url
        this
    }

    OfferAssert hasId(String id) {
        assert actual.id == id
        this
    }

    OfferAssert hasLocation(String location) {
        assert actual.location == location
        this
    }

    OfferAssert hasSchemaVersion(int version) {
        assert actual.schemaVersion == version
        this
    }

    OfferAssert hasSectionsSize(int size) {
        assert actual.sections.size() == size
        this
    }

    OfferAssert hasEmptySections() {
        assert actual.sections.isEmpty()
        this
    }
}

class OfferListAssert {
    private final List<JobOffer> actual

    private OfferListAssert(List<JobOffer> actual) {
        this.actual = actual
    }

    static OfferListAssert assertThat(List<JobOffer> actual) {
        new OfferListAssert(actual)
    }

    OfferListAssert hasSize(int size) {
        assert actual.size() == size
        this
    }

    OfferAssert first() {
        assert !actual.isEmpty()
        OfferAssert.assertThat(actual[0])
    }
}
