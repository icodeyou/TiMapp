package com.timappweb.timapp.data.loader;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Created by Stephane on 02/09/2016.
 */

public class SectionContainer{

    public enum LoadStatus {PENDING, DONE, ERROR}
    /**
     * Order by start id DESC
     */
    TreeSet<PaginatedSection> sections;
    private PaginateDirection order;
    public enum PaginateDirection {ASC, DESC};

    public PaginatedSection first() {
        return sections.size() > 0 ? sections.first() : null;
    }

    public PaginatedSection last(){
        return sections.size() > 0 ? sections.last() : null;
    }


    public SectionContainer() {
        this.sections = new TreeSet<>(new Comparator<PaginatedSection>() {
            @Override
            public int compare(PaginatedSection lhs, PaginatedSection rhs) {
                return lhs.start < rhs.start
                        ? (order == PaginateDirection.DESC ? -1 : 1)
                        : lhs.start == rhs.start
                            ? 0
                            : (order == PaginateDirection.DESC ? 1 : -1);
            }
        });
    }

    public void clear() {
        this.sections.clear();
    }


    public PaginateDirection getOrder() {
        return order;
    }

    public void setOrder(PaginateDirection order) {
        this.order = order;
    }

    /**
     * Returns true if the section is already loaded
     * @param start
     * @param end
     * @return
     */
    /*
    public List<Section> getSections(long start, long end){
        List<Section> results = new LinkedList();
        for (Section section: sections){
            if (section.start >= start && section.end >= end
                    || section.start <= start && section.end <= end){
                results.add(section);
            }
        }
        return results;
    }*/

    /**
     *
     * @param start
     * @return
     */
    public PaginatedSection findSection(long start) {
        if (start == -1 && sections.size() > 0){
            return sections.first();
        }
        for (PaginatedSection section: sections){
            if (section.start == start){
                return section;
            }
            else if (section.start < start){
                break;
            }
        }
        return null;
    }

    public PaginatedSection findOlderSection(PaginatedSection newSection) {
        return findSection(newSection.end  + (this.order == PaginateDirection.ASC ? -1 : 1));
    }



    /**
     * True if there is at least one section that is waiting for data
     * @return
     */
    public boolean isLoading(){
        for (PaginatedSection section: sections){
            if (section.isStatus(LoadStatus.PENDING)) return true;
        }
        return false;
    }

    /**
     * Returns true if the section is already loaded
     * @return
     */
    public boolean isLoaded(PaginatedSection section){
        if (section.isFirstLoad() && sections.size() > 0){
            return true;
        }
        for (PaginatedSection s: sections){
            if (s.contains(section.getStart(), section.getEnd())){
                return s.isStatus(LoadStatus.DONE);
            }
        }
        return false;
    }

    /**
     * Add a new section to the history, keep section ordered
     * @param newSection
     */
    public void addSection(PaginatedSection newSection){
        this.sections.add(newSection);
    }


    public static class PaginatedSection<T>{

        public  long        start;
        public  long        end;
        public  long        lastUpdate;
        public  LoadStatus  status;
        private PaginatedDataLoader.LoadType loadType;

        //public abstract List<T> getData();

        public PaginatedSection(long start, long end) {
            this.start = start;
            this.status = LoadStatus.PENDING;
            this.end = end;
        }

        public PaginatedSection(long start) {
            this.start = start;
            this.end = -1;
            this.status = LoadStatus.PENDING;
        }

        public boolean contains(long start, long end){
            return this.start >= start && this.end <= end;
        }

        public boolean isStatus(LoadStatus status) {
            return this.status == status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PaginatedSection<?> section = (PaginatedSection<?>) o;

            if (start != section.start) return false;
            return end == section.end;

        }

        @Override
        public int hashCode() {
            int result = (int) (start ^ (start >>> 32));
            result = 31 * result + (int) (end ^ (end >>> 32));
            return result;
        }

        public void setLoadType(PaginatedDataLoader.LoadType loadType) {
            this.loadType = loadType;
        }

        public PaginatedDataLoader.LoadType getLoadType() {
            return loadType;
        }

        public PaginatedSection<T> setStatus(LoadStatus status) {
            this.status = status;
            return this;
        }

        public void setStart(long minId) {
            this.start = minId;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public LoadStatus getStatus() {
            return status;
        }

        public boolean hasLoadError(){
            return status == LoadStatus.ERROR;
        }

        public long getEnd() {
            return end;
        }

        public long getStart() {
            return start;
        }

        public boolean isFirstLoad() {
            return this.end == -1 && this.start == -1;
        }

        @Override
        public String toString() {
            return "PaginatedSection{" +
                    "start=" + start +
                    ", end=" + end +
                    ", lastUpdate=" + lastUpdate +
                    ", status=" + status +
                    ", loadType=" + loadType +
                    '}';
        }


    }
}
